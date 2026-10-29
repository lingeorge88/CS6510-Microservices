package com.selfcheckout.catalog;

import com.selfcheckout.catalog.data.CatalogItem;
import com.selfcheckout.catalog.data.CatalogItemRepository;
import com.selfcheckout.events.CatalogItemEvent;
import com.selfcheckout.events.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes the whole catalog to the compacted {@code catalog.items} topic on every boot.
 * Idempotent (keyed by sku, compaction keeps the latest), so no outbox is needed.
 * Runs before the web server starts, so "port 8081 open" implies "catalog published".
 */
@Component
public class CatalogPublisher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CatalogPublisher.class);

    private final CatalogItemRepository catalog;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private volatile boolean running;

    public CatalogPublisher(CatalogItemRepository catalog, KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.catalog = catalog;
        this.kafka = kafka;
        this.json = json;
    }

    @Override
    public void start() {
        List<CatalogItem> items = catalog.findAll();
        CompletableFuture<?>[] sends = items.stream()
                .map(i -> kafka.send(Topics.CATALOG_ITEMS, i.getSku(),
                        json.writeValueAsString(new CatalogItemEvent(i.getSku(), i.getName(), i.getPrice()))))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(sends).join();
        log.info("Published {} catalog items to {}", items.size(), Topics.CATALOG_ITEMS);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /** Before the web server (phase ~Integer.MAX_VALUE - 2048) opens its port. */
    @Override
    public int getPhase() {
        return 0;
    }
}