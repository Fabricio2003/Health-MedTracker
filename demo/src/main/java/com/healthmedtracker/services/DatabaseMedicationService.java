package com.healthmedtracker.services;

import com.healthmedtracker.models.Medication;
import com.healthmedtracker.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMedicationService extends MedicationService {

    public DatabaseMedicationService() {
        loadAllMedications();
    }

    private void loadAllMedications() {
        String sql = "SELECT * FROM medication";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String dosage = rs.getString("dosage");
                int freq = rs.getInt("frequency");
                int duration = rs.getInt("duration_days");
                int qty = rs.getInt("quantity_per_bottle");
                String notes = rs.getString("notes");

                String startStr = rs.getString("start_date");
                LocalDate startDate = (startStr != null)
                        ? LocalDate.parse(startStr)
                        : LocalDate.now();

                List<LocalTime> times = loadDoseTimes(id);

                Medication med = new Medication(
                        id, name, dosage, freq, duration, qty, notes, startDate, times
                );

                super.addMedication(med);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LocalTime> loadDoseTimes(String medId) {
        List<LocalTime> times = new ArrayList<>();

        String sql = "SELECT time FROM dose_time WHERE med_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                times.add(LocalTime.parse(rs.getString("time")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return times;
    }

    @Override
public void removeMedication(String id) {
    // 1. Remove from SQL
    String deleteMed = "DELETE FROM medication WHERE id = ?";
    String deleteTimes = "DELETE FROM dose_time WHERE med_id = ?";

    try (Connection conn = DatabaseConnection.connect()) {

        // Delete dose times first (FK constraint)
        try (PreparedStatement stmt = conn.prepareStatement(deleteTimes)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }

        // Delete medication
        try (PreparedStatement stmt = conn.prepareStatement(deleteMed)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    // 2. Remove from in-memory list + file
    super.removeMedication(id);
}

    private void saveDoseTimes(Medication med) {
        String sql = "INSERT INTO dose_time (med_id, time) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (LocalTime t : med.getDoseSchedule()) {
                stmt.setString(1, med.getId());
                stmt.setString(2, t.toString());
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
