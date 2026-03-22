package com.healthmedtracker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import com.healthmedtracker.models.Medication;
import com.healthmedtracker.models.ScheduledDose;
import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ScheduleService;

public class App {
    public static void main(String[] args) {
    MedicationService medService = new MedicationService();

    List<LocalTime> amoxTimes = Arrays.asList(
            LocalTime.of(8, 0),   
            LocalTime.of(20, 0)   
        );
        
        List<LocalTime> vitDTimes = Arrays.asList(
            LocalTime.of(9, 30)  
        );

        
        Medication m1 = new Medication("1", "Amoxicillin", "500mg", 2, 10, 20, "Take with food", amoxTimes);
        Medication m2 = new Medication("2", "Vitamin D", "1000 IU", 1, 30, 30, "", vitDTimes);

    medService.addMedication(m1);
    medService.addMedication(m2);

    ScheduleService scheduleService = new ScheduleService();
    List<ScheduledDose> today = scheduleService.generateDailySchedule(
            medService.getAllMedications(),
            LocalDate.now()
    );
    System.out.println("--- TODAY'S SCHEDULE ---");
        for (ScheduledDose dose : today) {
            System.out.println(dose.getTime().toLocalTime() + " - " + dose.getMedication().getName());
        }
        System.out.println("------------------------\n");

        
        if (!today.isEmpty()) {
            today.get(0).markTaken();
        }

    AdherenceService adherenceService = new AdherenceService();
    double adherence = adherenceService.calculateDailyAdherence(today, LocalDate.now());

    System.out.println("Today's adherence: " + adherence * 100 + "%");
}

}
