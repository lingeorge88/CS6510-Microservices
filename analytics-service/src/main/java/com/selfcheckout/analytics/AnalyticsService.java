package com.selfcheckout.analytics;

import com.selfcheckout.analytics.data.PopularItemSnapshot;
import com.selfcheckout.analytics.data.PopularItemSnapshotRepository;
import com.selfcheckout.analytics.data.ScanEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

/**
 * Maintains and serves the popular-items hopping window: a scheduled task
 * recomputes the ranking over recent scans into a snapshot, and reads serve the
 * latest snapshot. All data is local to Analytics' own database.
 */
@Service
public class AnalyticsService {

    private final ScanEventRepository scanEvents;
    private final PopularItemSnapshotRepository snapshots;
    private final JdbcTemplate jdbc;

    private final int windowSize;
    private final int slideInterval;
    private final int popularItemsLimit;
    private final int snapshotDepth;

    public AnalyticsService(ScanEventRepository scanEvents,
                            PopularItemSnapshotRepository snapshots,
                            JdbcTemplate jdbc,
                            @Value("${selfcheckout.analytics.window-size:1000}") int windowSize,
                            @Value("${selfcheckout.analytics.slide-interval:500}") int slideInterval,
                            @Value("${selfcheckout.analytics.popular-items-limit:10}") int popularItemsLimit,
                            @Value("${selfcheckout.analytics.snapshot-depth:100}") int snapshotDepth) {
        this.scanEvents = scanEvents;
        this.snapshots = snapshots;
        this.jdbc = jdbc;
        this.windowSize = windowSize;
        this.slideInterval = slideInterval;
        this.popularItemsLimit = popularItemsLimit;
        this.snapshotDepth = snapshotDepth;
    }

    /** Recomputes the latest hopping window if a new slide boundary has been crossed. */
    @Scheduled(fixedDelayString = "${selfcheckout.analytics.recompute-interval-ms:500}")
    @Transactional
    public void recompute() {
        Long maxSeq = scanEvents.maxSequence();
        if (maxSeq == null) {
            return;
        }

        long boundary = (maxSeq / slideInterval) * slideInterval;
        if (boundary < windowSize) {
            return;
        }

        Long last = snapshots.latestWindowEnd();
        if (last != null && boundary <= last) {
            return;
        }

        long start = boundary - windowSize + 1;
        List<ScanEventRepository.RankedScanRow> ranked = scanEvents.rankWindow(start, boundary);

        LocalDateTime now = LocalDateTime.now();
        List<PopularItemSnapshot> rows = new ArrayList<>();
        int rank = 1;
        for (ScanEventRepository.RankedScanRow row : ranked) {
            if (rank > snapshotDepth) {
                break;
            }
            rows.add(new PopularItemSnapshot(start, boundary, now,
                    row.getSku(), row.getName(), (int) row.getScanCount(), rank));
            rank++;
        }
        jdbc.batchUpdate("""
                INSERT INTO popular_items_snapshot
                    (window_start, window_end, computed_at, sku, name, scan_count, rank)
                VALUES (?, ?, ?, ?, ?, ?, ?)""", rows, rows.size(), (ps, r) -> {
            ps.setLong(1, r.getWindowStart());
            ps.setLong(2, r.getWindowEnd());
            ps.setTimestamp(3, Timestamp.valueOf(r.getComputedAt()));
            ps.setString(4, r.getSku());
            ps.setString(5, r.getName());
            ps.setInt(6, r.getScanCount());
            ps.setInt(7, r.getRank());
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> popularItems(Integer limit) {
        int effectiveLimit = limit == null ? popularItemsLimit : limit;

        List<Map<String, Object>> items = new ArrayList<>();
        long windowStart = 0;
        long windowEnd = 0;
        String computedAt = LocalDateTime.now().toString();

        Long latest = snapshots.latestWindowEnd();
        if (latest != null) {
            for (PopularItemSnapshot row : snapshots.findByWindowEndOrderByRankAsc(latest)) {
                if (row.getRank() > effectiveLimit) {
                    break;
                }
                items.add(Map.of(
                        "sku", row.getSku(),
                        "name", row.getName(),
                        "scanCount", row.getScanCount(),
                        "rank", row.getRank()));
                windowStart = row.getWindowStart();
                windowEnd = row.getWindowEnd();
                computedAt = row.getComputedAt().toString();
            }
        }

        return Map.of(
                "windowSize", windowSize,
                "slideInterval", slideInterval,
                "windowStart", windowStart,
                "windowEnd", windowEnd,
                "computedAt", computedAt,
                "items", items);
    }
}
