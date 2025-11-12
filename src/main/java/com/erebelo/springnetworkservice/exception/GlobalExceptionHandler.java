package com.erebelo.springnetworkservice.exception;

import com.erebelo.springnetworkservice.exception.model.BadRequestException;
import com.erebelo.springnetworkservice.exception.model.ExceptionResponse;
import com.erebelo.springnetworkservice.exception.model.NotFoundException;
import com.erebelo.springnetworkservice.exception.model.S3MultipartUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleException(Exception exception) {
        log.error("Exception thrown:", exception);
        return createExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleBadRequestException(BadRequestException exception) {
        log.error("BadRequestException thrown:", exception);
        return createExceptionResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ExceptionResponse handleNotFoundException(NotFoundException exception) {
        log.error("NotFoundException thrown:", exception);
        return createExceptionResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(S3MultipartUploadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleS3MultipartUploadException(S3MultipartUploadException exception) {
        log.error("S3MultipartUploadException thrown:", exception);
        return createExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ExceptionResponse createExceptionResponse(final HttpStatus httpStatus, final String message) {
        String errorMessage = ObjectUtils.isEmpty(message) ? "No defined message" : message;
        return new ExceptionResponse(httpStatus, errorMessage, System.currentTimeMillis());
    }
}
