package com.healthmedtracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;



import com.healthmedtracker.models.Medication;
import com.healthmedtracker.models.ScheduledDose;
import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ReminderService;
import com.healthmedtracker.services.ScheduleService;

public class App {


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        MedicationService medService = new MedicationService();

        int medCounter = 1;
        while (true) {
            System.out.println("Add a medication? (y/n)");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("n")) break;

            String id = String.valueOf(medCounter++);
            Medication med = createMedicationFromUserInput(scanner, id);
            medService.addMedication(med);
        }

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

        while (true) {
            LocalDateTime now = LocalDateTime.now();
            List<ScheduledDose> due = reminderService.getUpcomingDoses(today, now);

            for (ScheduledDose dose : due) {
                System.out.println("\nReminder: " + dose.getMedication().getName() +
                                   " (" + dose.getMedication().getDosage() + ")");
                System.out.println("Time: " + dose.getTime().toLocalTime());
                System.out.println("[1] Take");
                System.out.println("[2] Skip");

                int action = Integer.parseInt(scanner.nextLine());

                if (action == 1) {
                    dose.markTaken();
                    System.out.println("Marked as taken");
                } else if (action == 2) {
                    dose.markSkipped();
                    System.out.println("Marked as skipped");
                }

                double updatedAdherence = adherenceService.calculateDailyAdherence(today, LocalDate.now());
                System.out.println("Updated adherence: " + (updatedAdherence * 100) + "%");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Medication createMedicationFromUserInput(Scanner scanner, String id) {
    System.out.print("Enter medication name: ");
    String name = scanner.nextLine();

    System.out.print("Enter dosage (e.g., 500mg): ");
    String dosage = scanner.nextLine();

    // NEW: Ask for frequency per day
    System.out.print("How many times per day will you take this medication? ");
    int frequencyPerDay = Integer.parseInt(scanner.nextLine());

    // NEW: Ask for duration in days
    System.out.print("For how many days will you take this medication? ");
    int durationDays = Integer.parseInt(scanner.nextLine());

    int quantity = 30; // You can later ask the user for this too
    String notes = "";

    // NEW: Support both 24-hour and AM/PM formats
    DateTimeFormatter formatter24 = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter formatter12 = DateTimeFormatter.ofPattern("hh:mm a");

    System.out.println("Enter the " + frequencyPerDay + " times you take this medication each day.");
    System.out.println("Use HH:mm or hh:mm AM/PM. Type 'done' when finished.");

    List<LocalTime> times = new java.util.ArrayList<>();

    while (times.size() < frequencyPerDay) {
        System.out.print("Time " + (times.size() + 1) + ": ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("done")) {
            System.out.println("You must enter " + frequencyPerDay + " times.");
            continue;
        }

        try {
            LocalTime time;

            // This line testa out the acceptance of the 24-hour format 
            try {
                time = LocalTime.parse(input, formatter24);
            } catch (Exception e1) {
                // This line testa out the acceptance of 12-hour format (AM/PM)
                time = LocalTime.parse(input.toUpperCase(), formatter12);
            }

            times.add(time);
            System.out.println("Added time: " + time);

        } catch (Exception e) {
            System.out.println("Invalid time. Use HH:mm or hh:mm AM/PM");
        }
    }

    return new Medication(id, name, dosage, frequencyPerDay, durationDays, quantity, notes, times);
}


}
