package com.erebelo.springnetworkservice.scheduler;

import com.erebelo.springnetworkservice.service.NetworkHydrationService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "network.hydration.scheduler.enabled", havingValue = "true")
public class NetworkHydrationScheduler {

    private final NetworkHydrationService service;

    @Scheduled(cron = "${network.hydration.scheduler.cron:0 0 3 * * MON-FRI}")
    @SchedulerLock(name = "networkHydrationScheduler", lockAtMostFor = "PT3H", lockAtLeastFor = "${network.hydration.scheduler.min-lock-time:PT1H}")
    public void networkHydrationScheduler() {
        log.info("Starting network hydration scheduler");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        service.hydrateNetworks(timestamp);
    }
}
