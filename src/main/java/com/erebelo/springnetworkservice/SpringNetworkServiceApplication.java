package com.erebelo.springnetworkservice;

import com.erebelo.springnetworkservice.config.NetworkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({NetworkProperties.class})
public class SpringNetworkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringNetworkServiceApplication.class, args);
    }
}
