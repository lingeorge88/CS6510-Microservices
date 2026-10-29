package com.selfcheckout.events.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Publishes committed outbox rows to Kafka, then deletes them. At-least-once: if the
 * process dies after send but before delete, rows are re-sent, so consumers dedupe.
 * Deletes by the exact ids sent (never "id <= max"): a lower id can commit later.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH = 500;

    private record Row(long id, String topic, String key, String payload) {
    }

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    public OutboxRelay(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
        this.jdbc = jdbc;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${selfcheckout.outbox.poll-interval-ms:10}")
    public void relay() {
        List<Row> rows;
        do {
            rows = jdbc.query("SELECT id, topic, msg_key, payload FROM outbox ORDER BY id LIMIT ?",
                    (rs, i) -> new Row(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)),
                    BATCH);
            if (rows.isEmpty()) {
                return;
            }
            CompletableFuture<?>[] sends = rows.stream()
                    .map(r -> kafka.send(r.topic(), r.key(), r.payload()))
                    .toArray(CompletableFuture[]::new);
            try {
                CompletableFuture.allOf(sends).join();
            } catch (CompletionException e) {
                log.warn("Outbox publish failed, will retry: {}", e.getCause().toString());
                return;
            }
            Long[] ids = rows.stream().map(Row::id).toArray(Long[]::new);
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("DELETE FROM outbox WHERE id = ANY(?)");
                ps.setArray(1, con.createArrayOf("bigint", ids));
                return ps;
            });
        } while (rows.size() == BATCH);
    }
}
