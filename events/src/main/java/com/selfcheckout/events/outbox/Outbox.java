package com.selfcheckout.events.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes an event into the local {@code outbox} table. MANDATORY propagation: it must
 * join the caller's DB transaction, so the event commits (or rolls back) atomically
 * with the business change. {@link OutboxRelay} later publishes it to Kafka.
 */
@Component
public class Outbox {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public Outbox(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void add(String topic, String key, Object event) {
        jdbc.update("INSERT INTO outbox (topic, msg_key, payload) VALUES (?, ?, ?)",
                topic, key, json.writeValueAsString(event));
    }
}