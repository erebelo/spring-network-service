package com.erebelo.springnetworkservice.controller;

import static com.erebelo.springnetworkservice.constant.BusinessConstant.NETWORKS_PATH;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.service.NetworkService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(NETWORKS_PATH)
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService service;

    @GetMapping(value = "/{rootReferenceId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<NetworkDto> getNetworkByRootReferenceId(
            @PathVariable("rootReferenceId") String rootReferenceId,
            @Valid @RequestParam(value = "relationshipDate", required = false) LocalDate relationshipDate) {
        log.info("GET {}/{}?relationshipDate={}", NETWORKS_PATH, rootReferenceId,
                relationshipDate != null ? relationshipDate : "");
        return ResponseEntity.ok(service.findByRootReferenceId(rootReferenceId, relationshipDate));
    }
}
