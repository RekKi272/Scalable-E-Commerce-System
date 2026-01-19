package com.hmkeyewear.product_service.scheduler;

import com.hmkeyewear.product_service.config.ElasticsearchHealthChecker;
import com.hmkeyewear.product_service.service.ProductSearchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSyncScheduler {

    private final ElasticsearchHealthChecker healthChecker;
    private final ProductSearchSyncService syncService;

    private boolean synced = false;

    @Scheduled(fixedDelay = 15000) // 15s
    public void syncWhenReady() {

        if (synced) return;

        if (healthChecker.isReady()) {
            log.info("Elasticsearch READY → start syncing");
            syncService.syncAll();
            synced = true;
        } else {
            log.info("Waiting for Elasticsearch...");
        }
    }
}
