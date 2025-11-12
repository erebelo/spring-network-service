package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.service.NetworkDecoratorService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkDecoratorServiceImpl implements NetworkDecoratorService {

    @Override
    public NetworkDto legacyNetworkDecorator(List<Relationship> relationships, String rootReferenceId,
            LocalDate relationshipDate) {
        return null;
    }
}
