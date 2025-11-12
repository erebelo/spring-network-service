package com.erebelo.springnetworkservice.service.aws.impl;

import com.erebelo.springnetworkservice.exception.model.S3MultipartUploadException;
import com.erebelo.springnetworkservice.service.aws.S3MultipartUploadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

@Slf4j
@Service
public class S3MultipartUploadServiceImpl implements S3MultipartUploadService {

    private final S3AsyncClient s3Client;
    private final Executor taskExecutor;
    private final Map<String, String> uploadIds = new ConcurrentHashMap<>();
    private final Map<String, List<CompletedPart>> uploadedParts = new ConcurrentHashMap<>();

    public S3MultipartUploadServiceImpl(S3AsyncClient s3Client,
            @Qualifier("s3AsyncTaskExecutor") Executor taskExecutor) {
        this.s3Client = s3Client;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public CompletableFuture<String> startMultipartUpload(String bucket, String key, String contentType) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder().bucket(bucket).key(key)
                .contentType(contentType).build();

        return CompletableFuture.supplyAsync(() -> s3Client.createMultipartUpload(request).join(), taskExecutor)
                .thenApply(response -> {
                    uploadIds.put(key, response.uploadId());
                    uploadedParts.put(key, Collections.synchronizedList(new ArrayList<>()));
                    log.info("Started multipart upload for key={}, uploadId={}", key, response.uploadId());
                    return response.uploadId();
                }).exceptionally(e -> {
                    throw new S3MultipartUploadException(String
                            .format("Failed to start multipart upload for key=%s. Error: %s", key, e.getMessage()), e);
                });
    }

    @Override
    public CompletableFuture<Void> uploadBatchPart(String bucket, String key, byte[] data, int partNumber) {
        String uploadId = uploadIds.get(key);
        if (uploadId == null) {
            throw new IllegalStateException("Multipart upload not started for key=" + key);
        }

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId)
                .partNumber(partNumber).build();

        return CompletableFuture
                .supplyAsync(() -> s3Client.uploadPart(uploadPartRequest, AsyncRequestBody.fromBytes(data)).join(),
                        taskExecutor)
                .thenAccept(response -> {
                    CompletedPart completedPart = CompletedPart.builder().partNumber(partNumber).eTag(response.eTag())
                            .build();
                    uploadedParts.get(key).add(completedPart);
                    log.info("Uploaded part-{} for key={}, eTag={}", partNumber, key, response.eTag());
                }).exceptionally(e -> {
                    throw new S3MultipartUploadException(String.format("Failed to upload part=%d for key=%s. Error: %s",
                            partNumber, key, e.getMessage()), e);
                });
    }

    @Override
    public CompletableFuture<Void> completeMultipartUpload(String bucket, String key) {
        String uploadId = uploadIds.remove(key);
        List<CompletedPart> parts = uploadedParts.remove(key);

        if (uploadId == null || parts == null) {
            throw new IllegalStateException("No multipart upload in progress for key=" + key);
        }

        if (parts.isEmpty()) {
            log.info("No multipart upload for key={}", key);
            return CompletableFuture.completedFuture(null);
        }

        // Parts must be sorted by partNumber before completion
        parts.sort(Comparator.comparingInt(CompletedPart::partNumber));

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder().bucket(bucket).key(key)
                .uploadId(uploadId).multipartUpload(u -> u.parts(parts)).build();

        return CompletableFuture.supplyAsync(() -> s3Client.completeMultipartUpload(request).join(), taskExecutor)
                .thenAccept(
                        resp -> log.info("Completed multipart upload for key={}, location={}", key, resp.location()))
                .exceptionally(e -> {
                    throw new S3MultipartUploadException(String.format(
                            "Failed to complete multipart upload for key=%s. Error: %s", key, e.getMessage()), e);
                });
    }

    /**
     * Aborts an ongoing multipart upload and cleans up local tracking maps.
     */
    @Override
    public CompletableFuture<Void> abortMultipartUpload(String bucket, String key) {
        String uploadId = uploadIds.remove(key);
        uploadedParts.remove(key);

        if (uploadId == null) {
            log.info("No active multipart upload to abort for key={}", key);
            return CompletableFuture.completedFuture(null);
        }

        AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder().bucket(bucket).key(key)
                .uploadId(uploadId).build();

        return CompletableFuture.supplyAsync(() -> s3Client.abortMultipartUpload(request).join(), taskExecutor)
                .thenAccept(resp -> log.info("Aborted multipart upload for key={}, uploadId={}", key, uploadId))
                .exceptionally(e -> {
                    throw new S3MultipartUploadException(String
                            .format("Failed to abort multipart upload for key=%s. Error: %s", key, e.getMessage()), e);
                });
    }
}
