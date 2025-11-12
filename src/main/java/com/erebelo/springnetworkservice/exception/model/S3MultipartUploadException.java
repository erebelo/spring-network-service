package com.erebelo.springnetworkservice.exception.model;

public class S3MultipartUploadException extends RuntimeException {

    public S3MultipartUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
