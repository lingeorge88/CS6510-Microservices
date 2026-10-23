package com.selfcheckout.analytics.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Data access for the scan_events table (Analytics' own database). */
public interface ScanEventRepository extends JpaRepository<ScanEvent, Long> {

    /** @return the highest scan id so far, or null if there are none */
    @Query("SELECT MAX(s.id) FROM ScanEvent s")
    Long maxSequence();

    /**
     * Ranks SKUs within the inclusive id window by scan count descending then SKU
     * ascending. The name is read straight from the denormalized column — no join
     * to the catalog's database, which no longer exists in this process.
     */
    @Query(value = """
            SELECT s.sku AS "sku", s.name AS "name", COUNT(*) AS "scanCount"
            FROM scan_events s
            WHERE s.id BETWEEN :start AND :end
            GROUP BY s.sku, s.name
            ORDER BY "scanCount" DESC, s.sku ASC
            """, nativeQuery = true)
    List<RankedScanRow> rankWindow(@Param("start") long start, @Param("end") long end);

    /** Projection for {@link #rankWindow}. */
    interface RankedScanRow {
        String getSku();

        String getName();

        long getScanCount();
    }
}
