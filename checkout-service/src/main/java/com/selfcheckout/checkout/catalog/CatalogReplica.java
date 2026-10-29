package com.selfcheckout.checkout.catalog;

import com.selfcheckout.events.CatalogItemEvent;
import com.selfcheckout.events.Topics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checkout's local, read-only copy of the catalog, built from the compacted
 * {@code catalog.items} topic (event-carried state transfer). Replaces the per-scan
 * gRPC GetItem: price lookup on scan is now a map get. Loads before the web server
 * starts, so "port 8082 open" implies "replica ready".
 */
@Component
public class CatalogReplica implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CatalogReplica.class);

    public record Item(String name, BigDecimal price) {
    }

    private final Map<String, Item> items = new ConcurrentHashMap<>();
    private final ConsumerFactory<String, String> consumerFactory;
    private final JsonMapper json;
    private volatile boolean running;

    public CatalogReplica(ConsumerFactory<String, String> consumerFactory, JsonMapper json) {
        this.consumerFactory = consumerFactory;
        this.json = json;
    }

    public Optional<Item> find(String sku) {
        return Optional.ofNullable(items.get(sku));
    }

    /** Reads the topic from the beginning to its current end, then returns. */
    @Override
    public void start() {
        Properties props = new Properties();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        try (Consumer<String, String> consumer = consumerFactory.createConsumer(null, "catalog-replica", null, props)) {
            List<TopicPartition> partitions = consumer.partitionsFor(Topics.CATALOG_ITEMS).stream()
                    .map(p -> new TopicPartition(p.topic(), p.partition()))
                    .toList();
            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
            Map<TopicPartition, Long> end = consumer.endOffsets(partitions);
            while (partitions.stream().anyMatch(tp -> consumer.position(tp) < end.get(tp))) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(200))) {
                    CatalogItemEvent event = json.readValue(record.value(), CatalogItemEvent.class);
                    items.put(event.sku(), new Item(event.name(), event.price()));
                }
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException(Topics.CATALOG_ITEMS + " is empty; is catalog-service up?");
        }
        log.info("Catalog replica loaded: {} items", items.size());
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

    /** Before the web server opens its port. */
    @Override
    public int getPhase() {
        return 0;
    }
}
