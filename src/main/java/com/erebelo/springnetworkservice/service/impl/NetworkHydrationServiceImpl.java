package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.config.NetworkProperties;
import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.dto.NetworkHydrationContextDto;
import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.exception.model.S3MultipartUploadException;
import com.erebelo.springnetworkservice.repository.NetworkHydrationRepository;
import com.erebelo.springnetworkservice.service.NetworkDecoratorService;
import com.erebelo.springnetworkservice.service.NetworkHydrationService;
import com.erebelo.springnetworkservice.service.NetworkTraversalService;
import com.erebelo.springnetworkservice.service.aws.S3MultipartUploadService;
import com.erebelo.springnetworkservice.util.UploadSerializerUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

@Slf4j
@Service
public class NetworkHydrationServiceImpl implements NetworkHydrationService {

    private final NetworkProperties networkProperties;
    private final NetworkTraversalService networkTraversalService;
    private final NetworkDecoratorService networkDecoratorService;
    private final S3MultipartUploadService s3MultipartUploadService;
    private final NetworkHydrationRepository repository;
    private final Executor taskExecutor;
    private final DataSize s3UploadBatchSize;
    private final String bucketName;

    private static final String NETWORKS_KEY = "networks/networks.json";
    private static final String NETWORKS_CONTENT_TYPE = "application/json";
    private static final String ERRORS_KEY = "networks/errors.csv";
    private static final String ERRORS_CONTENT_TYPE = "text/csv";

    public NetworkHydrationServiceImpl(NetworkProperties networkProperties,
            NetworkTraversalService networkTraversalService, NetworkDecoratorService networkDecoratorService,
            S3MultipartUploadService s3MultipartUploadService, NetworkHydrationRepository repository,
            @Qualifier("networkAsyncTaskExecutor") Executor taskExecutor,
            @Value("${network.hydration.s3.upload.batch-size:50MB}") DataSize s3UploadBatchSize,
            @Value("${s3.network.upload.bucket-name:}") String bucketName) {
        this.networkProperties = networkProperties;
        this.networkTraversalService = networkTraversalService;
        this.networkDecoratorService = networkDecoratorService;
        this.s3MultipartUploadService = s3MultipartUploadService;
        this.repository = repository;
        this.taskExecutor = taskExecutor;
        this.s3UploadBatchSize = s3UploadBatchSize;
        this.bucketName = bucketName;
    }

    @Override
    public String triggerNetworkHydration() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        CompletableFuture.runAsync(() -> hydrateNetworks(timestamp), taskExecutor).exceptionally(ex -> {
            log.error(ex.getMessage(), ex);
            return null;
        });

        return timestamp;
    }

    @Override
    public void hydrateNetworks(String timestamp) {
        log.info("Starting to hydrate networks with timestamp key suffix={}", timestamp);
        long startTime = System.nanoTime();

        NetworkHydrationContextDto context = new NetworkHydrationContextDto(insertTimestamp(NETWORKS_KEY, timestamp),
                insertTimestamp(ERRORS_KEY, timestamp));

        try {
            CompletableFuture.allOf(
                    s3MultipartUploadService.startMultipartUpload(bucketName, context.getNetworksKey(),
                            NETWORKS_CONTENT_TYPE),
                    s3MultipartUploadService.startMultipartUpload(bucketName, context.getErrorsKey(),
                            ERRORS_CONTENT_TYPE))
                    .join();

            networkProperties.getRootCriteria().forEach(c -> criteriaBatchProcess(c, context));

            // Final flush of remainder
            flushIfNeeded(true, context);

            CompletableFuture
                    .allOf(s3MultipartUploadService.completeMultipartUpload(bucketName, context.getNetworksKey()),
                            s3MultipartUploadService.completeMultipartUpload(bucketName, context.getErrorsKey()))
                    .join();
        } catch (Exception e) {
            CompletableFuture.allOf(s3MultipartUploadService.abortMultipartUpload(bucketName, context.getNetworksKey()),
                    s3MultipartUploadService.abortMultipartUpload(bucketName, context.getErrorsKey())).join();
            throw e;
        }

        long duration = Math.round((System.nanoTime() - startTime) / 1_000_000_000.0);
        log.info("Network hydration completed in {} seconds", duration);
    }

    private void criteriaBatchProcess(RoleEnum role, NetworkHydrationContextDto context) {
        log.info("Starting to discovery networks for role={}", role.getValue());
        Semaphore semaphore = new Semaphore(20);

        try (Stream<String> referenceIds = repository.streamReferenceIdBatchByRole(role)) {

            List<CompletableFuture<Void>> futures = referenceIds.map((String referenceId) -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while acquiring semaphore", e);
                }

                return CompletableFuture.runAsync(() -> {
                    try {
                        List<Relationship> relationships = networkTraversalService
                                .deriveNetworkFromRootReferenceId(referenceId);
                        NetworkDto network = networkDecoratorService.legacyNetworkDecorator(relationships, referenceId,
                                null);

                        if (network.getSellingRelationships() != null) {
                            addNetwork(network, context);
                        } else {
                            addError(referenceId, "No selling relationships found for referenceId=" + referenceId,
                                    context);
                        }
                    } catch (S3MultipartUploadException s3e) {
                        log.error("An error occurred while hydrating networks for role={}. Aborting network hydration.",
                                role.getValue(), s3e);
                        throw s3e;
                    } catch (Exception e) {
                        // For errors that occur during network discovery, no exception will be thrown,
                        // but rather the error will be tracked
                        addError(referenceId, e.getMessage(), context);
                    } finally {
                        semaphore.release();
                    }
                }, taskExecutor);
            }).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    /**
     * Add network: read-lock so flusher can block writers reliably.
     */
    private void addNetwork(NetworkDto network, NetworkHydrationContextDto context) {
        context.getRwLock().readLock().lock();
        try {
            context.getNetworks().add(network);
            context.getNetworkBytes().addAndGet(UploadSerializerUtil.toJsonLines(network).length);
        } finally {
            context.getRwLock().readLock().unlock();
        }

        flushIfNeeded(false, context);
    }

    /**
     * Add error: read-lock so flusher can block writers reliably.
     */
    private void addError(String adbId, String message, NetworkHydrationContextDto context) {
        context.getRwLock().readLock().lock();
        try {
            context.getErrors().put(adbId, message);
            context.getErrorBytes().addAndGet(UploadSerializerUtil.toCsvLines(Map.of(adbId, message)).length);
        } finally {
            context.getRwLock().readLock().unlock();
        }

        flushIfNeeded(false, context);
    }

    /**
     * This method blocks (writers will be blocked) while upload runs because it
     * acquires write lock.
     * 
     * <pre>
     * - finalFlush=false: flush only when either networkBytes or errorBytes >= S3 upload batch size.
     * - finalFlush=true: flush anything remaining (if any).
     * </pre>
     */
    private void flushIfNeeded(boolean finalFlush, NetworkHydrationContextDto context) {
        boolean performFinalFlush = finalFlush
                && (context.getNetworkBytes().get() > 0 || context.getErrorBytes().get() > 0);
        boolean reachedBatchLimit = context.getNetworkBytes().get() >= s3UploadBatchSize.toBytes()
                || context.getErrorBytes().get() >= s3UploadBatchSize.toBytes();

        if (!performFinalFlush && !reachedBatchLimit) {
            return;
        }

        context.getRwLock().writeLock().lock();
        try {
            if ((performFinalFlush && context.getNetworkBytes().get() > 0)
                    || context.getNetworkBytes().get() >= s3UploadBatchSize.toBytes()) {
                flushNetworks(context);
            }
            if ((performFinalFlush && context.getErrorBytes().get() > 0)
                    || context.getErrorBytes().get() >= s3UploadBatchSize.toBytes()) {
                flushErrors(context);
            }
        } catch (Exception e) {
            throw new S3MultipartUploadException("S3 Multipart Upload failed. Error: " + e.getMessage(), e);
        } finally {
            context.getRwLock().writeLock().unlock();
        }
    }

    private void flushNetworks(NetworkHydrationContextDto context) {
        // Drain the networks queue (build snapshot)
        List<NetworkDto> networksSnapshot = new ArrayList<>(context.getNetworks().size());
        NetworkDto n;
        while ((n = context.getNetworks().poll()) != null) {
            networksSnapshot.add(n);
        }

        // Reset counter
        context.getNetworkBytes().set(0);

        log.info("Uploading networks batch file with {} records", networksSnapshot.size());
        byte[] networkData = UploadSerializerUtil.toJsonLines(networksSnapshot);

        s3MultipartUploadService.uploadBatchPart(bucketName, context.getNetworksKey(), networkData,
                context.getNetworkPartCounter().incrementAndGet()).join();
    }

    private void flushErrors(NetworkHydrationContextDto context) {
        // Clear the errors map (build snapshot)
        Map<String, String> errorsSnapshot = new HashMap<>(context.getErrors());
        context.getErrors().clear();

        // Reset counter
        context.getErrorBytes().set(0);

        log.info("Uploading errors batch file with {} records", errorsSnapshot.size());
        byte[] errorsData = UploadSerializerUtil.toCsvLines(errorsSnapshot);

        s3MultipartUploadService.uploadBatchPart(bucketName, context.getErrorsKey(), errorsData,
                context.getErrorPartCounter().incrementAndGet()).join();
    }

    private String insertTimestamp(String key, String timestamp) {
        int dotIndex = key.lastIndexOf(".");
        if (dotIndex == -1) {
            return key + "_" + timestamp; // no extension case
        }
        return key.substring(0, dotIndex) + "_" + timestamp + key.substring(dotIndex);
    }
}
