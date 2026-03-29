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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
   
    private final Color[] MED_COLORS = {
    new Color(0x1D9E75), // teal
    new Color(0xD85A30), // coral
    new Color(0x3C3489), // slate
    new Color(0xF4A300), // amber
    new Color(0x0080FF)  // blue
};

    private Color getColorForMedication(String medId) {
    int index = Math.abs(medId.hashCode()) % MED_COLORS.length;
    return MED_COLORS[index];
     }


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
    private int getTimesPerDay(JTextField freqField) {
    try {
        return Integer.parseInt(freqField.getText().trim());
    } catch (Exception e) {
        return -1; // invalid or empty
    }
}


    private void applyAdherenceToSchedule() {
    for (ScheduledDose dose : todaySchedule) {
        List<AdherenceRecord> recs =
                adherenceService.getRecordsForMedication(dose.getMedication().getId());

        for (AdherenceRecord r : recs) {
            if (r.getScheduledTime().toLocalTime().equals(dose.getTime().toLocalTime())) {
                if (r.getStatus() == AdherenceRecord.Status.TAKEN) {
                    dose.markTaken();
                } else if (r.getStatus() == AdherenceRecord.Status.MISSED) {
                    dose.markSkipped();
                }
            }
        }
    }
}


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
        startReminderThread();
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
        title.setForeground(Color.BLACK);
        JLabel dateLabel = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(new Color(0xE1F5EE));
        header.add(title,    BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);

        // Tabs
        tabs = new JTabbedPane();
        tabs.setFont(FONT_BODY);
        tabs.setBackground(BG);
        tabs.addTab("  Medications  ", buildMedicationsTab());
        tabs.addTab("  Schedule     ", buildScheduleTab());
        tabs.addTab("  Adherence    ", buildAdherenceTab());
        tabs.addTab("  History      ", buildHistoryTab());
        tabs.addTab("  Calendar     ", buildCalendarTab());

        // Refresh relevant data when user switches tabs
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            switch (idx) {
                case 1 -> refreshSchedule();
                case 2 -> refreshAdherence();
                case 3 -> refreshHistory();
                case 4 -> refreshCalendar();
    }
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
            if (row < 0) {
                showInfo("Select a medication first.");
                return;
            }
            String id = (String) medTableModel.getValueAt(row, 0);

            // Remove medication only; do NOT renumber IDs (keeps adherence/history stable)
            medService.removeMedication(id);
            

            // Refresh meds table
            refreshMedications();

            // Regenerate today's schedule so deleted med disappears from Schedule tab
            todaySchedule = scheduleService.generateDailySchedule(
                    medService.getAllMedications(), LocalDate.now());
            applyAdherenceToSchedule();
            refreshCalendar();
            refreshSchedule();
        });

        refreshMedications();
        return panel;
    }

    private void refreshMedications() {
        medTableModel.setRowCount(0);
        for (Medication m : medService.getAllMedications()) {
            medTableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getDosage(),
                    m.getFrequencyPerDay(), m.getDurationDays() + " days",
                    m.getQuantityPerBottle()
            });
        }
    }

    private void showAddMedicationDialog() {
    JDialog dialog = new JDialog(this, "Add Medication", true);
    dialog.setSize(420, 520);
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

    // ⭐ Dynamic time list panel
    JPanel timesPanel = new JPanel();
    timesPanel.setLayout(new BoxLayout(timesPanel, BoxLayout.Y_AXIS));
    timesPanel.setBackground(BG);

    JButton addTimeBtn = primaryButton("+ Add Time");

    // Add time button logic
    addTimeBtn.addActionListener(ev -> {
          int max = getTimesPerDay(freqF);

         if (max <= 0) {
        showError("Please enter a valid 'Times per day' value first.");
        return;
        }

        // Count existing time rows
        int currentCount = 0;
        for (Component c : timesPanel.getComponents()) {
        if (c instanceof JPanel) currentCount++;
        }

        if (currentCount >= max) {
        showError("You cannot add more than " + max + " times.");
        return;
        }
        JTextField tf = formField();
        tf.setPreferredSize(new Dimension(120, 28));

        JButton removeBtn = dangerButton("X");
        removeBtn.setPreferredSize(new Dimension(45, 28));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(BG);
        row.add(tf);
        row.add(removeBtn);

        removeBtn.addActionListener(e2 -> {
            timesPanel.remove(row);
            timesPanel.revalidate();
            timesPanel.repaint();
        });

        timesPanel.add(row);
        timesPanel.revalidate();
        timesPanel.repaint();
    });

    // Layout fields
    String[] labels = {
        "Medication name", "Dosage (e.g. 500mg)", "Quantity (pills)",
        "Times per day", "Duration (days)", "Dose Times"
    };
    JComponent[] fields = {nameF, doseF, qtyF, freqF, durF, timesPanel};

    for (int i = 0; i < labels.length; i++) {
        gc.gridy = i * 2;
        JLabel lbl = new JLabel(labels[i]);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MED);
        form.add(lbl, gc);

        gc.gridy = i * 2 + 1;
        if (i == 5) {
            // Times panel + Add button
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(BG);
            wrapper.add(timesPanel, BorderLayout.CENTER);
            wrapper.add(addTimeBtn, BorderLayout.SOUTH);
            form.add(wrapper, gc);
        } else {
            form.add(fields[i], gc);
        }
    }

    // ⭐ Time normalization helper
    DateTimeFormatter fmt24 = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter fmt12 = DateTimeFormatter.ofPattern("hh:mm a");

    JButton save = primaryButton("Save");
    save.addActionListener(e -> {
        try {
            String id = java.util.UUID.randomUUID().toString();
            String name = nameF.getText().trim();
            String dos  = doseF.getText().trim();
            int qty     = Integer.parseInt(qtyF.getText().trim());
            int freq    = Integer.parseInt(freqF.getText().trim());
            int dur     = Integer.parseInt(durF.getText().trim());

            List<LocalTime> times = new ArrayList<>();

            for (Component c : timesPanel.getComponents()) {
                if (c instanceof JPanel row) {
                    JTextField tf = (JTextField) row.getComponent(0);
                    String raw = tf.getText().trim().toUpperCase();

                    //  Auto-correct H:mm → HH:mm
                    if (raw.matches("^[0-9]:[0-9]{2}.*")) {
                        raw = "0" + raw;
                    }

                    // Normalize AM/PM spacing
                    raw = raw.replaceAll("(?i)(AM)$", " AM");
                    raw = raw.replaceAll("(?i)(PM)$", " PM");

                    LocalTime parsed;

                    try {
                        parsed = LocalTime.parse(raw, fmt24);
                    } catch (Exception e1) {
                        try {
                            parsed = LocalTime.parse(raw, fmt12);
                        } catch (Exception e2) {
                            throw new IllegalArgumentException("Invalid time: " + raw);
                        }
                    }

                    times.add(parsed);
                }
            }

            if (times.size() != freq) {
                throw new IllegalArgumentException(
                    "You entered " + times.size() + " times, but frequency is " + freq + "."
                );
            }

            medService.addMedication(
                new Medication(id, name, dos, freq, dur, qty, "", LocalDate.now(), times)
            );

            refreshMedications();
            todaySchedule = scheduleService.generateDailySchedule(
                medService.getAllMedications(), LocalDate.now()
            );
            applyAdherenceToSchedule();
            refreshCalendar();
            refreshSchedule();

            dialog.dispose();

        } catch (Exception ex) {
            showError("Please check your input.\n" + ex.getMessage());
        }
    });

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnRow.setBackground(BG);
    btnRow.setBorder(new EmptyBorder(0, 16, 12, 16));
    btnRow.add(save);

    dialog.add(new JScrollPane(form,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
        BorderLayout.CENTER);

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
        if (tabs.getSelectedIndex() != 1) {
        return;
    }
        if (todaySchedule.isEmpty() && tabs.getSelectedIndex() == 1) {
            todaySchedule = scheduleService.generateDailySchedule(
                    medService.getAllMedications(), LocalDate.now());
            applyAdherenceToSchedule();
        }
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
            // IMPORTANT: do NOT reset notified here, or reminders will re-fire
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
//  Tab 5 — Calendar View
// ================================================================

    private JPanel calendarGrid;
    private JLabel monthLabel;
    private LocalDate calendarViewDate = LocalDate.now();
    private LocalDate lastKnownToday = LocalDate.now();
    private JTabbedPane tabs;


    private JPanel buildCalendarTab() {
      JPanel panel = new JPanel(new BorderLayout(15, 15));
     panel.setBackground(BG);
     panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    // --- Header (Month / Year Navigation) ---
     JPanel header = new JPanel(new BorderLayout());
     header.setBackground(BG);

      monthLabel = new JLabel("", SwingConstants.CENTER);
      monthLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
      monthLabel.setForeground(SLATE);

      JButton prev = ghostButton("  ◀ Previous  ");
      JButton next = ghostButton("  Next ▶  ");

      prev.addActionListener(e -> {
        calendarViewDate = calendarViewDate.minusMonths(1);
        refreshCalendar();
    });

    next.addActionListener(e -> {
        calendarViewDate = calendarViewDate.plusMonths(1);
        refreshCalendar();
    });

    header.add(prev, BorderLayout.WEST);
    header.add(monthLabel, BorderLayout.CENTER);
    header.add(next, BorderLayout.EAST);

    // --- Grid Container ---
    calendarGrid = new JPanel(new GridLayout(0, 7, 8, 8));
    calendarGrid.setBackground(BG);

    panel.add(header, BorderLayout.NORTH);
    panel.add(new JScrollPane(calendarGrid,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
            BorderLayout.CENTER);

    refreshCalendar();
    return panel;
}

    public void refreshCalendar() {
    calendarGrid.removeAll();
    monthLabel.setText(calendarViewDate.getMonth().toString() + " " + calendarViewDate.getYear());

    // Day Headers (SUN, MON, etc.)
    String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    for (String d : days) {
        JLabel l = new JLabel(d, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT_MED);
        calendarGrid.add(l);
    }

    LocalDate firstOfMonth = calendarViewDate.withDayOfMonth(1);
    int skip = firstOfMonth.getDayOfWeek().getValue() % 7;

    // Empty slots for previous month
    for (int i = 0; i < skip; i++) {
        JPanel empty = new JPanel();
        empty.setBackground(BG);
        calendarGrid.add(empty);
    }

    int daysInMonth = calendarViewDate.lengthOfMonth();
    for (int i = 1; i <= daysInMonth; i++) {
        LocalDate date = calendarViewDate.withDayOfMonth(i);
        calendarGrid.add(createDayPanel(date));
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
}

   private JPanel createDayPanel(LocalDate date) {
    JPanel box = new JPanel();
    box.setLayout(new BorderLayout());
    box.setBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0)));
    box.setBackground(Color.WHITE);

    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
    dayLabel.setFont(FONT_BODY);
    dayLabel.setForeground(TEXT_DARK);
    box.add(dayLabel, BorderLayout.NORTH);
    List<ScheduledDose> doses = scheduleService.generateDailySchedule(medService.getAllMedications(), date);
    Map<String, List<ScheduledDose>> grouped = new LinkedHashMap<>();
    for (ScheduledDose d : doses) {
        String medName = d.getMedication().getName();
        grouped.putIfAbsent(medName, new ArrayList<>());
        grouped.get(medName).add(d);
    }
    

    JPanel timesList = new JPanel();
    timesList.setLayout(new BoxLayout(timesList, BoxLayout.Y_AXIS));
    timesList.setBackground(Color.WHITE);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");

    for (String medName : grouped.keySet()) {

        Medication med = grouped.get(medName).get(0).getMedication();

        // Determine if medication is active on this date
        LocalDate start = med.getStartDate();
        if (start == null) {
           return box;
            }// If you store start date, replace this
        LocalDate end = start.plusDays(med.getDurationDays() - 1);

        if (date.isBefore(start) || date.isAfter(end)) {
            continue; // Skip inactive days
        }

        // Medication name
        JLabel medLabel = new JLabel(medName);
        medLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        medLabel.setForeground(getColorForMedication(med.getId()));
        timesList.add(medLabel);

        // Times
        for (ScheduledDose d : grouped.get(medName)) {
            JLabel timeLabel = new JLabel("• " + d.getTime().toLocalTime().format(fmt));
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            timeLabel.setForeground(getColorForMedication(med.getId()));
            timesList.add(timeLabel);
        }

        timesList.add(Box.createVerticalStrut(4)); // spacing
    }
    box.add(timesList, BorderLayout.CENTER);
    return box;
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
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(CORAL);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(new Color(0xE8E6DE));
        b.setForeground(TEXT_DARK);
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

    // ================================================================
    //  Reminder thread
    // ================================================================

    private void startReminderThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate today = LocalDate.now();
                // Only auto-refresh calendar if the actual day changed
                if (!today.equals(lastKnownToday)) {
                    lastKnownToday = today;

                  SwingUtilities.invokeLater(() -> {
                // Optional: only jump calendar if user is currently viewing today's month
                if (calendarViewDate.getMonth().equals(today.getMonth()) &&
                       calendarViewDate.getYear() == today.getYear()) {
                         refreshCalendar();
        }
    });
}


                    if (todaySchedule.isEmpty()) {
                        todaySchedule = scheduleService.generateDailySchedule(
                                medService.getAllMedications(), LocalDate.now());
                    }

                    List<ScheduledDose> due = reminderService.getUpcomingDoses(todaySchedule, now);

                    for (ScheduledDose dose : due) {
                        if (!dose.isNotified()) {
                            dose.markNotified();
                            SwingUtilities.invokeLater(() -> showReminderPopup(dose));
                        }
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void showReminderPopup(ScheduledDose dose) {
        JDialog dialog = new JDialog(this, "Medication Reminder", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JLabel msg = new JLabel(
                "<html><center><b>" + dose.getMedication().getName() + "</b><br>" +
                dose.getMedication().getDosage() + "<br>" +
                "Time: " + dose.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")) +
                "</center></html>",
                SwingConstants.CENTER
        );

        JButton take = primaryButton("Take");
        JButton skip = dangerButton("Skip");

        take.addActionListener(e -> {
            adherenceService.recordTaken(dose);
            dialog.dispose();
            refreshSchedule();
        });

        skip.addActionListener(e -> {
            adherenceService.recordMissed(dose);
            dialog.dispose();
            refreshSchedule();
        });

        JPanel btns = new JPanel();
        btns.add(take);
        btns.add(skip);

        dialog.add(msg, BorderLayout.CENTER);
        dialog.add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
