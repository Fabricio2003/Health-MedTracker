package com.healthmedtracker.ui;

import com.healthmedtracker.models.Medication;
import com.healthmedtracker.models.ScheduledDose;
import com.healthmedtracker.models.AdherenceRecord;
import com.healthmedtracker.services.AdherenceService;
import com.healthmedtracker.services.HistoryService;
import com.healthmedtracker.services.MedicationService;
import com.healthmedtracker.services.ReminderService;
import com.healthmedtracker.services.ScheduleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MainWindow — Section 1 UI (Binyam)
 *
 * A Swing tabbed window that ties together all backend services.
 * Four tabs:
 *   1. Medications  — add / remove / view all medications
 *   2. Schedule     — today's doses with Take / Miss buttons
 *   3. Adherence    — live adherence rate for each medication
 *   4. History      — full sorted log of every dose event
 */
public class MainWindow extends JFrame {

    // --- palette (soft teal / slate / coral matching your design doc) ---
    private static final Color TEAL       = new Color(0x1D9E75);
    private static final Color TEAL_LIGHT = new Color(0xE1F5EE);
    private static final Color CORAL      = new Color(0xD85A30);
    private static final Color SLATE      = new Color(0x3C3489);
    private static final Color BG         = new Color(0xF8F9FA);
    private static final Color TEXT_DARK  = new Color(0x2C2C2A);
    private static final Color TEXT_MED   = new Color(0x5F5E5A);
    private static final Font  FONT_HEAD  = new Font("SansSerif", Font.BOLD,   15);
    private static final Font  FONT_BODY  = new Font("SansSerif", Font.PLAIN,  13);
    private static final Font  FONT_SMALL = new Font("SansSerif", Font.PLAIN,  12);

    // --- services (all injected from App.java) ---
    private final MedicationService  medService;
    private final ScheduleService    scheduleService;
    private final AdherenceService   adherenceService;
    private final HistoryService     historyService;
    private final ReminderService    reminderService;

    // --- today's schedule (refreshed when the Schedule tab opens) ---
    private List<ScheduledDose> todaySchedule = new ArrayList<>();

    // --- table models (updated on refresh) ---
    private DefaultTableModel medTableModel;
    private DefaultTableModel scheduleTableModel;
    private DefaultTableModel adherenceTableModel;
    private DefaultTableModel historyTableModel;

    // --- live adherence label at top of Adherence tab ---
    private JLabel adherenceSummaryLabel;

    public MainWindow(MedicationService medService,
                      ScheduleService scheduleService,
                      AdherenceService adherenceService,
                      HistoryService historyService,
                      ReminderService reminderService) {
        this.medService       = medService;
        this.scheduleService  = scheduleService;
        this.adherenceService = adherenceService;
        this.historyService   = historyService;
        this.reminderService  = reminderService;

        buildWindow();
    }

    // ================================================================
    //  Window scaffold
    // ================================================================

    private void buildWindow() {
        setTitle("Health MedTracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 580);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TEAL);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Health MedTracker");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel dateLabel = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(new Color(0xE1F5EE));
        header.add(title,    BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BODY);
        tabs.setBackground(BG);
        tabs.addTab("  Medications  ", buildMedicationsTab());
        tabs.addTab("  Schedule     ", buildScheduleTab());
        tabs.addTab("  Adherence    ", buildAdherenceTab());
        tabs.addTab("  History      ", buildHistoryTab());

        // Refresh relevant data when user switches tabs
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) refreshSchedule();
            if (idx == 2) refreshAdherence();
            if (idx == 3) refreshHistory();
        });

        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }

    // ================================================================
    //  Tab 1 — Medications
    // ================================================================

    private JPanel buildMedicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Table
        medTableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Dosage", "Freq/day", "Duration", "Qty"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(medTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Button bar
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(BG);
        JButton addBtn    = primaryButton("+ Add Medication");
        JButton removeBtn = dangerButton("Remove Selected");
        buttons.add(addBtn);
        buttons.add(removeBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddMedicationDialog());
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Select a medication first."); return; }
            String id = (String) medTableModel.getValueAt(row, 0);
            medService.removeMedication(id);
            refreshMedications();
        });

        refreshMedications();
        return panel;
    }

    private void refreshMedications() {
        medTableModel.setRowCount(0);
        for (Medication m : medService.getAllMedications()) {
            medTableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getDosage(),
                    m.getfrequencyPerDay(), m.getDurationDays() + " days",
                    m.getQuantityPerBottle()
            });
        }
    }

    private void showAddMedicationDialog() {
        JDialog dialog = new JDialog(this, "Add Medication", true);
        dialog.setSize(400, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 4, 6, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField nameF  = formField();
        JTextField doseF  = formField();
        JTextField qtyF   = formField();
        JTextField freqF  = formField();
        JTextField durF   = formField();
        JTextField timesF = formField();
        timesF.setToolTipText("e.g. 08:00, 14:00");

        String[][] rows = {
                {"Medication name",      null},
                {"Dosage (e.g. 500mg)",  null},
                {"Quantity (pills)",     null},
                {"Times per day",        null},
                {"Duration (days)",      null},
                {"Dose times (HH:mm, comma-separated)", null}
        };
        JTextField[] fields = {nameF, doseF, qtyF, freqF, durF, timesF};

        for (int i = 0; i < rows.length; i++) {
            gc.gridy = i * 2;     gc.gridx = 0;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(FONT_SMALL); lbl.setForeground(TEXT_MED);
            form.add(lbl, gc);
            gc.gridy = i * 2 + 1; gc.gridx = 0;
            form.add(fields[i], gc);
        }

        JButton save = primaryButton("Save");
        save.addActionListener(e -> {
            try {
                String id   = String.valueOf(medService.getAllMedications().size() + 1);
                String name = nameF.getText().trim();
                String dos  = doseF.getText().trim();
                int    qty  = Integer.parseInt(qtyF.getText().trim());
                int    freq = Integer.parseInt(freqF.getText().trim());
                int    dur  = Integer.parseInt(durF.getText().trim());

                List<LocalTime> times = new ArrayList<>();
                for (String t : timesF.getText().split(",")) {
                    times.add(LocalTime.parse(t.trim(),
                            DateTimeFormatter.ofPattern("HH:mm")));
                }

                medService.addMedication(
                        new Medication(id, name, dos, freq, dur, qty, "", times));
                refreshMedications();
                dialog.dispose();
            } catch (Exception ex) {
                showError("Please check your input.\n" + ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(BG);
        btnRow.setBorder(new EmptyBorder(0, 16, 12, 16));
        btnRow.add(save);

        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ================================================================
    //  Tab 2 — Schedule
    // ================================================================

    private JPanel buildScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        scheduleTableModel = new DefaultTableModel(
                new String[]{"Time", "Medication", "Dosage", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(scheduleTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(BG);
        JButton takeBtn  = primaryButton("Mark as Taken");
        JButton missBtn  = dangerButton("Mark as Missed");
        JButton refreshB = ghostButton("Refresh");
        buttons.add(takeBtn);
        buttons.add(missBtn);
        buttons.add(refreshB);
        panel.add(buttons, BorderLayout.SOUTH);

        takeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Select a dose first."); return; }
            ScheduledDose dose = todaySchedule.get(row);
            adherenceService.recordTaken(dose);
            refreshSchedule();
        });
        missBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Select a dose first."); return; }
            ScheduledDose dose = todaySchedule.get(row);
            adherenceService.recordMissed(dose);
            refreshSchedule();
        });
        refreshB.addActionListener(e -> refreshSchedule());

        return panel;
    }

    private void refreshSchedule() {
        todaySchedule = scheduleService.generateDailySchedule(
                medService.getAllMedications(), LocalDate.now());
        scheduleTableModel.setRowCount(0);
        for (ScheduledDose dose : todaySchedule) {
            String status = dose.isTaken()   ? "✓ Taken"
                          : dose.isSkipped() ? "✗ Missed"
                          : "Pending";
            scheduleTableModel.addRow(new Object[]{
                    dose.getTime().toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm a")),
                    dose.getMedication().getName(),
                    dose.getMedication().getDosage(),
                    status
            });
        }
    }

    // ================================================================
    //  Tab 3 — Adherence
    // ================================================================

    private JPanel buildAdherenceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Summary card at top
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TEAL_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL, 1, true),
                new EmptyBorder(14, 18, 14, 18)));
        adherenceSummaryLabel = new JLabel("—");
        adherenceSummaryLabel.setFont(FONT_HEAD);
        adherenceSummaryLabel.setForeground(new Color(0x0F6E56));
        card.add(adherenceSummaryLabel, BorderLayout.CENTER);
        panel.add(card, BorderLayout.NORTH);

        // Per-medication table
        adherenceTableModel = new DefaultTableModel(
                new String[]{"Medication", "Taken", "Missed", "Adherence %"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(adherenceTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void refreshAdherence() {
        // Today's overall summary
        if (todaySchedule.isEmpty()) {
            todaySchedule = scheduleService.generateDailySchedule(
                    medService.getAllMedications(), LocalDate.now());
        }
        adherenceSummaryLabel.setText(
                adherenceService.getDailySummary(todaySchedule, LocalDate.now()));

        // Per-medication breakdown
        adherenceTableModel.setRowCount(0);
        for (Medication m : medService.getAllMedications()) {
            List<AdherenceRecord> recs = adherenceService.getRecordsForMedication(m.getId());
            long taken  = recs.stream().filter(r -> r.getStatus() == AdherenceRecord.Status.TAKEN).count();
            long missed = recs.stream().filter(r -> r.getStatus() == AdherenceRecord.Status.MISSED).count();
            int  pct    = recs.isEmpty() ? 0
                        : (int) Math.round((double) taken / recs.size() * 100);
            adherenceTableModel.addRow(new Object[]{
                    m.getName(), taken, missed, pct + "%"
            });
        }
    }

    // ================================================================
    //  Tab 4 — History
    // ================================================================

    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        historyTableModel = new DefaultTableModel(
                new String[]{"Date", "Time", "Medication", "Status", "Taken at"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(historyTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(BG);
        JButton refreshB = ghostButton("Refresh");
        buttons.add(refreshB);
        refreshB.addActionListener(e -> refreshHistory());
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshHistory() {
        historyTableModel.setRowCount(0);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");
        for (AdherenceRecord r : historyService.getFullHistory()) {
            String takenAt = (r.getActualTime() != null)
                    ? r.getActualTime().toLocalTime().format(timeFmt) : "—";
            historyTableModel.addRow(new Object[]{
                    r.getScheduledTime().toLocalDate().format(dateFmt),
                    r.getScheduledTime().toLocalTime().format(timeFmt),
                    r.getMedicationName(),
                    r.getStatus().toString(),
                    takenAt
            });
        }
    }

    // ================================================================
    //  Helpers — styled components
    // ================================================================

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_BODY);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0x9FE1CB));
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(FONT_SMALL);
        t.getTableHeader().setBackground(new Color(0xE8E6DE));
        t.getTableHeader().setForeground(TEXT_MED);
        t.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        t.setGridColor(new Color(0xEEEDE8));
        return t;
    }

    private JTextField formField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB4B2A9), 1, true),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(TEAL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(CORAL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(BG);
        b.setForeground(TEXT_MED);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB4B2A9), 1, true),
                new EmptyBorder(7, 14, 7, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}