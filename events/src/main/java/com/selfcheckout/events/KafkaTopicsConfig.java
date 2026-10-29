package com.selfcheckout.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declares every topic; Spring's KafkaAdmin creates any that are missing at startup. */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    NewTopic catalogItemsTopic() {
        return TopicBuilder.name(Topics.CATALOG_ITEMS).partitions(1).replicas(1).compact().build();
    }

    @Bean
    NewTopic itemScannedTopic() {
        return TopicBuilder.name(Topics.ITEM_SCANNED).partitions(6).replicas(1).build();
    }

    @Bean
    NewTopic transactionCompletedTopic() {
        return TopicBuilder.name(Topics.TRANSACTION_COMPLETED).partitions(6).replicas(1).build();
    }

    @Bean
    NewTopic stockShortfallTopic() {
        return TopicBuilder.name(Topics.STOCK_SHORTFALL).partitions(6).replicas(1).build();
    }
}
