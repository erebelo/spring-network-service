package com.erebelo.springnetworkservice.service.aws;

import java.util.concurrent.CompletableFuture;

public interface S3MultipartUploadService {

    CompletableFuture<String> startMultipartUpload(String bucket, String key, String contentType);

    CompletableFuture<Void> uploadBatchPart(String bucket, String key, byte[] data, int partNumber);

    CompletableFuture<Void> completeMultipartUpload(String bucket, String key);

    CompletableFuture<Void> abortMultipartUpload(String bucket, String key);

}
