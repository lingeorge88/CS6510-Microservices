package com.selfcheckout.events.outbox;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Import this into a service that publishes events: outbox writer + relay. */
@Configuration
@EnableScheduling
@Import({Outbox.class, OutboxRelay.class})
public class OutboxConfig {
}
