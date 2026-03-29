package com.healthmedtracker.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.io.Serializable;
public class Medication implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String dosage;
    private int frequencyPerDay;
    private int durationDays;
    private int quantityPerBottle;
    private String notes;
    private LocalDate startDate;
    private List<LocalTime> doseSchedule;

    public Medication(String id, String name, String dosage, int frequencyPerDay,
                      int durationDays, int quantityPerBottle, String notes,
                      LocalDate startDate, List<LocalTime> doseSchedule) {

        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequencyPerDay = frequencyPerDay;
        this.durationDays = durationDays;
        this.quantityPerBottle = quantityPerBottle;
        this.notes = notes;
        this.startDate = startDate;
        this.doseSchedule = doseSchedule;
    }

    // getters...
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public int getFrequencyPerDay() { return frequencyPerDay; }
    public int getDurationDays() { return durationDays; }
    public int getQuantityPerBottle() { return quantityPerBottle; }
    public String getNotes() { return notes; }
    public LocalDate getStartDate() { return startDate; }
    public List<LocalTime> getDoseSchedule() { return doseSchedule; }
}
