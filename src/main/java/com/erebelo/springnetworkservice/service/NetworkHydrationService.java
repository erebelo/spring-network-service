package com.erebelo.springnetworkservice.service;

public interface NetworkHydrationService {

    String triggerNetworkHydration();

    void hydrateNetworks(String timestamp);

}
