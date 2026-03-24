package com.healthmedtracker.services;

import com.healthmedtracker.models.AdherenceRecord;
import com.healthmedtracker.models.AdherenceRecord.Status;
import com.healthmedtracker.models.ScheduledDose;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdherenceService — Section 4 (Binyam)
 *
 * Tracks whether each scheduled dose was taken or missed.
 * Works alongside the existing MedicationService and ScheduleService
 * by consuming ScheduledDose objects and producing AdherenceRecords.
 */
public class AdherenceService {

    // In-memory history of every dose event this session
    private final List<AdherenceRecord> records = new ArrayList<>();

    // ----------------------------------------------------------------
    //  Core actions (called when user taps Take / Skip in the UI)
    // ----------------------------------------------------------------

    /**
     * Record a dose as TAKEN right now.
     */
    public AdherenceRecord recordTaken(ScheduledDose dose) {
        return recordTaken(dose, LocalDateTime.now());
    }

    /**
     * Record a dose as TAKEN at a specific time (useful for testing).
     */
    public AdherenceRecord recordTaken(ScheduledDose dose, LocalDateTime takenAt) {
        dose.markTaken();
        AdherenceRecord record = new AdherenceRecord(
                dose.getMedication().getId(),
                dose.getMedication().getName(),
                dose.getTime()
        );
        record.markTaken(takenAt);
        records.add(record);
        return record;
    }

    /**
     * Record a dose as MISSED.
     */
    public AdherenceRecord recordMissed(ScheduledDose dose) {
        dose.markSkipped();
        AdherenceRecord record = new AdherenceRecord(
                dose.getMedication().getId(),
                dose.getMedication().getName(),
                dose.getTime()
        );
        record.markMissed();
        records.add(record);
        return record;
    }

    // ----------------------------------------------------------------
    //  Daily adherence calculation (kept from original, now richer)
    // ----------------------------------------------------------------

    /**
     * Original method — kept so App.java still works without changes.
     * Returns a 0.0-1.0 ratio of doses taken vs total scheduled for a given day.
     */
    public double calculateDailyAdherence(List<ScheduledDose> schedule, LocalDate date) {
        long total = schedule.stream()
                .filter(d -> d.getTime().toLocalDate().equals(date))
                .count();
        if (total == 0) return 1.0;
        long taken = schedule.stream()
                .filter(d -> d.getTime().toLocalDate().equals(date))
                .filter(ScheduledDose::isTaken)
                .count();
        return (double) taken / total;
    }

    /**
     * Returns the adherence rate (0.0-1.0) for one specific medication
     * across all stored records.
     */
    public double getAdherenceRateForMedication(String medicationId) {
        List<AdherenceRecord> medRecords = getRecordsForMedication(medicationId);
        if (medRecords.isEmpty()) return 1.0;
        long taken = medRecords.stream()
                .filter(r -> r.getStatus() == Status.TAKEN)
                .count();
        return (double) taken / medRecords.size();
    }

    // ----------------------------------------------------------------
    //  Querying records (used by the UI panels)
    // ----------------------------------------------------------------

    /** All records ever stored this session. */
    public List<AdherenceRecord> getAllRecords() {
        return new ArrayList<>(records);
    }

    /** Records for one medication. */
    public List<AdherenceRecord> getRecordsForMedication(String medicationId) {
        return records.stream()
                .filter(r -> r.getMedicationId().equals(medicationId))
                .collect(Collectors.toList());
    }

    /** Records for a specific date. */
    public List<AdherenceRecord> getRecordsForDate(LocalDate date) {
        return records.stream()
                .filter(r -> r.getScheduledTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /** Records between two dates (inclusive). */
    public List<AdherenceRecord> getRecordsInRange(LocalDate from, LocalDate to) {
        return records.stream()
                .filter(r -> {
                    LocalDate d = r.getScheduledTime().toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    /** Only the missed doses — useful for a "what did I miss?" view. */
    public List<AdherenceRecord> getMissedDoses() {
        return records.stream()
                .filter(r -> r.getStatus() == Status.MISSED)
                .collect(Collectors.toList());
    }

    /** Summary string for printing/displaying today's adherence. */
    public String getDailySummary(List<ScheduledDose> schedule, LocalDate date) {
        double rate = calculateDailyAdherence(schedule, date);
        long total = schedule.stream()
                .filter(d -> d.getTime().toLocalDate().equals(date)).count();
        long taken = Math.round(rate * total);
        int pct = (int) Math.round(rate * 100);
        return String.format("Today's adherence: %d%% (%d/%d doses taken)", pct, taken, total);
    }
}