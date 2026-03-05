package com.healthmedtracker.models;

public class Medication {
    
    private String name;
    private String dosage;
    private String frequency;
    private int durationDays;
    private int quantityPerBottle;
    private String notes;

    public Medication(String name, String dosage, String frequency,
                      int durationDays, int quantityPerBottle, String notes) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.quantityPerBottle = quantityPerBottle;
        this.notes = notes;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public int getDurationDays() { return durationDays; }
    public int getQuantityPerBottle() { return quantityPerBottle; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return name + " (" + dosage + ", " + frequency + ")";
    }
}

    

