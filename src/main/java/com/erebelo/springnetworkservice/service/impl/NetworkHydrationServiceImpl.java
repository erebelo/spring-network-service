package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.service.NetworkHydrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NetworkHydrationServiceImpl implements NetworkHydrationService {

    @Override
    public String triggerNetworkHydration() {
        return "";
    }

    @Override
    public void hydrateNetworks(String timestamp) {
    }
}
