package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.service.NetworkService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class NetworkServiceImpl implements NetworkService {

    @Override
    public NetworkDto findByRootReferenceId(String rootReferenceId, LocalDate relationshipDate) {
        return null;
    }
}
