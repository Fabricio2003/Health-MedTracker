package com.healthmedtracker.models;

public class Medication {
    private String id;
    private String name;
    private String dosage;
    private int frequencyPerDay;
    private int durationDays;
    private int quantityPerBottle;
    private String notes;

    public Medication(String id, String name, String dosage, int frequencyPerDay,
            int durationDays, int quantityPerBottle, String notes) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequencyPerDay = frequencyPerDay;
        this.durationDays = durationDays;
        this.quantityPerBottle = quantityPerBottle;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public int getfrequencyPerDay() {
        return frequencyPerDay;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public int getQuantityPerBottle() {
        return quantityPerBottle;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return name + " (" + dosage + ", " + frequencyPerDay + "x/day)";
    }
}
