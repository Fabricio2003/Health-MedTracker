package com.healthmedtracker;

import java.time.LocalDate;
import java.util.List;

import com.healthmedtracker.models.Medication;
import com.healthmedtracker.models.ScheduledDose;
import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ScheduleService;

public class App {
    public static void main(String[] args) {
    MedicationService medService = new MedicationService();

    Medication m1 = new Medication("1", "Amoxicillin", "500mg", 2, 10, 20, "Take with food");
    Medication m2 = new Medication("2", "Vitamin D", "1000 IU", 1, 30, 30, "");

    medService.addMedication(m1);
    medService.addMedication(m2);

    ScheduleService scheduleService = new ScheduleService();
    List<ScheduledDose> today = scheduleService.generateDailySchedule(
            medService.getAllMedications(),
            LocalDate.now()
    );

    // Mark first dose taken
    today.get(0).markTaken();

    AdherenceService adherenceService = new AdherenceService();
    double adherence = adherenceService.calculateDailyAdherence(today, LocalDate.now());

    System.out.println("Today's adherence: " + adherence * 100 + "%");
}

}
