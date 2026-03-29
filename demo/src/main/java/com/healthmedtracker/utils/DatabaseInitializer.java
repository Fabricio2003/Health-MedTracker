package com.healthmedtracker.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS medication (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                dosage TEXT,
                frequency INTEGER NOT NULL CHECK(frequency >= 0),
                duration_days INTEGER,
                quantity_per_bottle INTEGER,
                notes TEXT,
                start_date TEXT
            );
        """;

        String sqlTimes = """
            CREATE TABLE IF NOT EXISTS dose_time (
                med_id TEXT NOT NULL,
                time TEXT NOT NULL,
                FOREIGN KEY (med_id) REFERENCES medication(id)
            );
        """;

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            stmt.execute(sqlTimes);
            System.out.println("Database ready!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
