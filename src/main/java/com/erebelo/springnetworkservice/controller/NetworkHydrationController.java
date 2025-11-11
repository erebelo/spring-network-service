package com.erebelo.springnetworkservice.controller;

import static com.erebelo.springnetworkservice.constant.BusinessConstant.NETWORKS_HYDRATION_PATH;
import static com.erebelo.springnetworkservice.constant.BusinessConstant.NETWORKS_PATH;

import com.erebelo.springnetworkservice.service.NetworkHydrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(NETWORKS_PATH)
@RequiredArgsConstructor
public class NetworkHydrationController {

    private final NetworkHydrationService service;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = NETWORKS_HYDRATION_PATH, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> triggerNetworkHydration() {
        log.info("POST {}", NETWORKS_PATH + NETWORKS_HYDRATION_PATH);
        String keySuffix = service.triggerNetworkHydration();
        return ResponseEntity.accepted().body("Network hydration started with timestamp key suffix: " + keySuffix);
    }
}
