package com.selfcheckout.analytics;

import com.selfcheckout.events.ItemScanned;
import com.selfcheckout.events.Topics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * Ingests scan events into the local scan log in batches (one multi-row INSERT per
 * poll instead of one synchronous gRPC call + INSERT per scan). concurrency = 1 keeps
 * identity ids, which define the hopping window, contiguous per commit.
 */
@Component
public class ScanEventListener {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public ScanEventListener(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @KafkaListener(topics = Topics.ITEM_SCANNED, groupId = "analytics-service",
            batch = "true", concurrency = "1")
    @Transactional
    public void onScans(List<String> payloads) {
        List<ItemScanned> events = payloads.stream().map(p -> json.readValue(p, ItemScanned.class)).toList();
        jdbc.batchUpdate("""
                INSERT INTO scan_events (event_id, sku, name, scanned_at) VALUES (?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING""", events, 500, (ps, e) -> {
            ps.setObject(1, e.eventId());
            ps.setString(2, e.sku());
            ps.setString(3, e.name());
            ps.setTimestamp(4, Timestamp.valueOf(e.scannedAt()));
        });
    }
}
