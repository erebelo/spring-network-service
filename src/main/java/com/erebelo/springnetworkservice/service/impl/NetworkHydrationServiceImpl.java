package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.service.NetworkHydrationService;
import org.springframework.stereotype.Service;

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
