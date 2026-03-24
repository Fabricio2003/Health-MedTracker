package com.healthmedtracker.services;

import com.healthmedtracker.models.AdherenceRecord;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HistoryService — Section 4 (Binyam)
 *
 * Sits on top of AdherenceService and provides higher-level
 * history queries: sorted views, date-range filtering, and
 * per-medication summaries for display in the History panel.
 */
public class HistoryService {

    private final AdherenceService adherenceService;

    public HistoryService(AdherenceService adherenceService) {
        this.adherenceService = adherenceService;
    }

    /**
     * All records, sorted newest first — what the History tab shows by default.
     */
    public List<AdherenceRecord> getFullHistory() {
        return adherenceService.getAllRecords().stream()
                .sorted(Comparator.comparing(AdherenceRecord::getScheduledTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * History for one specific medication, newest first.
     */
    public List<AdherenceRecord> getHistoryForMedication(String medicationId) {
        return adherenceService.getRecordsForMedication(medicationId).stream()
                .sorted(Comparator.comparing(AdherenceRecord::getScheduledTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * History filtered to a date range, newest first.
     */
    public List<AdherenceRecord> getHistoryInRange(LocalDate from, LocalDate to) {
        return adherenceService.getRecordsInRange(from, to).stream()
                .sorted(Comparator.comparing(AdherenceRecord::getScheduledTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns a human-readable summary string for one medication.
     * Example: "Ibuprofen: 8/10 doses taken (80%)"
     */
    public String getMedicationSummary(String medicationId, String medicationName) {
        List<AdherenceRecord> records = adherenceService.getRecordsForMedication(medicationId);
        if (records.isEmpty()) return medicationName + ": no history yet";

        long total = records.size();
        long taken = records.stream()
                .filter(r -> r.getStatus() == AdherenceRecord.Status.TAKEN)
                .count();
        int pct = (int) Math.round((double) taken / total * 100);
        return String.format("%s: %d/%d doses taken (%d%%)", medicationName, taken, total, pct);
    }
}