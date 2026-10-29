package com.selfcheckout.events;

/** Kafka topic names: the event channels between services. */
public final class Topics {

    /** Compacted, key = sku. Catalog's full snapshot; checkout builds its price replica from it. */
    public static final String CATALOG_ITEMS = "catalog.items";
    /** key = transactionId. One event per scanned unit; feeds analytics. */
    public static final String ITEM_SCANNED = "checkout.item-scanned";
    /** key = transactionId. One event per completed basket; inventory decrements stock. */
    public static final String TRANSACTION_COMPLETED = "checkout.transaction-completed";
    /** key = transactionId. Units inventory could not fulfil; checkout compensates. */
    public static final String STOCK_SHORTFALL = "inventory.stock-shortfall";

    private Topics() {
    }
}
