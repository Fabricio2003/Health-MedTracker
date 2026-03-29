package com.healthmedtracker;

import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.DatabaseMedicationService;
import com.healthmedtracker.services.HistoryService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ReminderService;
import com.healthmedtracker.services.ScheduleService;
import com.healthmedtracker.ui.MainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.healthmedtracker.utils.DatabaseInitializer;

public class App {

    public static void main(String[] args) {
        DatabaseInitializer.init();

        System.out.println("App started");
        // Use the system look-and-feel so it feels native on any OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Wire up all services
        MedicationService medService = new DatabaseMedicationService();
        ScheduleService    scheduleService  = new ScheduleService();
        AdherenceService   adherenceService = new AdherenceService();
        HistoryService     historyService   = new HistoryService(adherenceService);
        ReminderService    reminderService  = new ReminderService();

        // Launch the window on the Swing event thread (standard Swing practice)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(
                    medService, scheduleService,
                    adherenceService, historyService, reminderService);
            window.setVisible(true);
        });
        System.out.println("DB path: " + new java.io.File("project.db").getAbsolutePath());
        System.out.println("Loaded meds: " + medService.getAllMedications().size());
        System.out.println("refreshCalendar called from: " + Thread.currentThread().getName());



    }
}