package com.erebelo.springnetworkservice.domain.dto;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class NetworkHydrationContextDto {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ConcurrentLinkedQueue<NetworkDto> networks = new ConcurrentLinkedQueue<>();
    private final AtomicLong networkBytes = new AtomicLong();
    private final AtomicInteger networkPartCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<String, String> errors = new ConcurrentHashMap<>();
    private final AtomicLong errorBytes = new AtomicLong();
    private final AtomicInteger errorPartCounter = new AtomicInteger(0);
    private final String networksKey;
    private final String errorsKey;

}
