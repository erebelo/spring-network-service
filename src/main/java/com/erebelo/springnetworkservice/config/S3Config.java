package com.erebelo.springnetworkservice.config;

import java.time.Duration;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class S3Config {

    @Bean
    public S3AsyncClient s3AsyncClient(@Qualifier("s3AsyncTaskExecutor") Executor s3TaskExecutor) {
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder().maxConcurrency(200)
                .maxPendingConnectionAcquires(1000).connectionAcquisitionTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofMinutes(5)).writeTimeout(Duration.ofMinutes(5)).build();

        ClientAsyncConfiguration asyncConfig = ClientAsyncConfiguration.builder()
                .advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR, s3TaskExecutor).build();

        return S3AsyncClient.builder().httpClient(httpClient).asyncConfiguration(asyncConfig).region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
    }
}
