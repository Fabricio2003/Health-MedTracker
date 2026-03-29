package com.healthmedtracker.models;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Medication {
    private String id;
    private String name;
    private String dosage;
    private int frequencyPerDay;
    private int durationDays;
    private int quantityPerBottle;
    private String notes;
    private List<LocalTime> doseSchedule;
    private LocalDate startDate;


    public Medication(String id, String name, String dosage, int frequencyPerDay,
            int durationDays, int quantityPerBottle, LocalDate startDate, List<LocalTime> doseSchedule) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequencyPerDay = frequencyPerDay;
        this.durationDays = durationDays;
        this.quantityPerBottle = quantityPerBottle;
        this.doseSchedule = doseSchedule;
        this.startDate = startDate;
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

    public List <LocalTime> getDoseSchedule(){
        return doseSchedule;
    }
    public void setId(String id) {
         this.id = id;
    }
    public LocalDate getStartDate() {
       return startDate;
   }



    @Override
    public String toString() {
        return name + " (" + dosage + ", " + frequencyPerDay + "x/day at " + doseSchedule + ")";
    }
}
