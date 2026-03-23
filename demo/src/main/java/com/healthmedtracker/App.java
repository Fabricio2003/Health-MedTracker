package com.healthmedtracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.healthmedtracker.models.Medication;
import com.healthmedtracker.models.ScheduledDose;
import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ReminderService;
import com.healthmedtracker.services.ScheduleService;

public class App {
    @SuppressWarnings("resource")
    public static void main(String[] args) {

        MedicationService medService = new MedicationService();

        // Test dose: fires 10 seconds from now
        List<LocalTime> amoxTimes = Arrays.asList(
            LocalTime.now().plusSeconds(10).withNano(0)
        );
        // Test dose: fires 30 seconds from now
        List<LocalTime> vitDTimes = Arrays.asList(
            LocalTime.now().plusSeconds(30).withNano(0)
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


        AdherenceService adherenceService = new AdherenceService();
        double adherence = adherenceService.calculateDailyAdherence(today, LocalDate.now());
        System.out.println("Today's adherence: " + adherence * 100 + "%");

        ReminderService reminderService = new ReminderService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            LocalDateTime now = LocalDateTime.now();
            List<ScheduledDose> due = reminderService.getUpcomingDoses(today, now);

            for (ScheduledDose dose : due) {
                System.out.println("\nReminder: " + dose.getMedication().getName() +
                                   " (" + dose.getMedication().getDosage() + ")");
                System.out.println("Time: " + dose.getTime().toLocalTime());
                System.out.println("[1] Take");
                System.out.println("[2] Skip");

                int choice = scanner.nextInt();

                if (choice == 1) {
                    dose.markTaken();
                    System.out.println("Marked as taken");
                } else if (choice == 2) {
                    dose.markSkipped();
                    System.out.println("Marked as skipped");
                }
                double updatedAdherence = adherenceService.calculateDailyAdherence(today, LocalDate.now());
                System.out.println("Updated adherence: " + (updatedAdherence * 100) + "%");

            }

            try {
                Thread.sleep(1000); // check every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

