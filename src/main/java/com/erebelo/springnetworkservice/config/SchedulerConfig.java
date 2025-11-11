package com.erebelo.springnetworkservice.config;

import com.mongodb.client.MongoCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.bson.Document;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT3H")
@ConditionalOnProperty(name = "network.hydration.scheduler.enabled", havingValue = "true")
public class SchedulerConfig {

    /*
     * Executor dedicated for scheduled tasks (@Scheduled methods). By having a
     * dedicated ThreadPoolTaskScheduler, scheduled jobs run on their own threads
     * and are isolated from manual async executions, avoiding conflicts and thread
     * starvation.
     */
    @Bean
    public TaskScheduler taskSchedulerPool() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("Sch-Executor-");
        scheduler.setDaemon(false);
        scheduler.setVirtualThreads(false);
        scheduler.setTaskDecorator(new ContextCopyingTaskDecorator());
        scheduler.setErrorHandler(new SchedulerErrorHandler());
        return scheduler;
    }

    @Bean
    public LockProvider lockProvider(MongoTemplate mongo) {
        MongoCollection<Document> schedulerLock = mongo.getCollection("scheduler_lock");
        return new MongoLockProvider(schedulerLock);
    }

    static class ContextCopyingTaskDecorator implements TaskDecorator {
        @Override
        public @NonNull Runnable decorate(@NonNull Runnable runnable) {
            final Map<String, String> loggingContext = MDC.getCopyOfContextMap();
            final Map<String, String> headers = HeaderContextHolder.get();

            return () -> {
                try {
                    // Restore MDC context for logging
                    if (loggingContext != null) {
                        MDC.setContextMap(loggingContext);
                    }

                    // Reuse or initialize header context
                    Map<String, String> headerContext = headers != null ? new HashMap<>(headers) : new HashMap<>();

                    // Add unique scheduled task ID
                    String scheduledTaskId = UUID.randomUUID().toString();
                    headerContext.put("scheduledTaskId", scheduledTaskId);
                    HeaderContextHolder.set(headerContext);

                    // Also add to MDC for log correlation
                    MDC.put("scheduledTaskId", scheduledTaskId);

                    runnable.run();
                } finally {
                    HeaderContextHolder.clear();
                    MDC.clear();
                }
            };
        }
    }

    static final class HeaderContextHolder {
        private static final ThreadLocal<Map<String, String>> CONTEXT = new ThreadLocal<>();

        private HeaderContextHolder() {
        }

        static void set(Map<String, String> headers) {
            CONTEXT.set(headers);
        }

        static Map<String, String> get() {
            return CONTEXT.get();
        }

        static void clear() {
            CONTEXT.remove();
        }
    }

    @Slf4j
    static class SchedulerErrorHandler implements ErrorHandler {
        @Override
        public void handleError(@NonNull Throwable t) {
            log.error("Something went wrong when processing a scheduled task");
            log.error(t.getMessage(), t);
        }
    }
}
