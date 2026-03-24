package com.healthmedtracker.models;

import java.time.LocalDateTime;

/**
 * Represents a single dose event — either taken or missed.
 * Every time a scheduled dose is acted on (taken or skipped),
 * one AdherenceRecord is created and stored.
 */
public class AdherenceRecord {

    public enum Status {
        TAKEN,
        MISSED,
        PENDING
    }

    private final String medicationId;
    private final String medicationName;
    private final LocalDateTime scheduledTime;
    private LocalDateTime actualTime;   // null if missed
    private Status status;

    public AdherenceRecord(String medicationId, String medicationName,
                           LocalDateTime scheduledTime) {
        this.medicationId   = medicationId;
        this.medicationName = medicationName;
        this.scheduledTime  = scheduledTime;
        this.status         = Status.PENDING;
    }

    // --- actions ---

    public void markTaken(LocalDateTime takenAt) {
        this.actualTime = takenAt;
        this.status     = Status.TAKEN;
    }

    public void markMissed() {
        this.actualTime = null;
        this.status     = Status.MISSED;
    }

    // --- getters ---

    public String getMedicationId()   { return medicationId; }
    public String getMedicationName() { return medicationName; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public LocalDateTime getActualTime()    { return actualTime; }
    public Status getStatus()               { return status; }

    @Override
    public String toString() {
        String timeStr = (actualTime != null)
                ? actualTime.toLocalTime().toString()
                : "—";
        return String.format("[%s] %s — scheduled %s, taken %s",
                status, medicationName,
                scheduledTime.toLocalTime(), timeStr);
    }
}