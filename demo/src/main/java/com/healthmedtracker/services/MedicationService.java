package com.healthmedtracker.services;

import com.healthmedtracker.models.Medication;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MedicationService {

    private static final String FILE_NAME = "medications.dat";

    private final List<Medication> medications;

    public MedicationService() {
        medications = loadFromFile();
    }

    public void addMedication(Medication med) {
        validMedication(med);
        medications.add(med);
        saveToFile();
    }

    public void updateMedication(String id, Medication updateMed) {
        if (updateMed == null) {
            throw new IllegalArgumentException("Updated medication can't be null!");
        }

        for (int i = 0; i < medications.size(); i++) {
            if (medications.get(i).getId().equals(id)) {

                if (!id.equals(updateMed.getId()) && findById(updateMed.getId()).isPresent()) {
                    throw new IllegalArgumentException(
                            "A medication with ID '" + updateMed.getId() + "' already exists."
                    );
                }

                validateMedicationFields(updateMed);
                medications.set(i, updateMed);
                saveToFile();
                return;
            }
        }

        throw new IllegalArgumentException("Medication with ID '" + id + "' not found.");
    }

    public List<Medication> getAllMedications() {
        return new ArrayList<>(medications);
    }

    public Optional<Medication> findById(String id) {
        if (id == null) return Optional.empty();

        return medications.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    public List<Medication> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerCaseName = name.toLowerCase();

        return medications.stream()
                .filter(m -> m.getName().toLowerCase().contains(lowerCaseName))
                .collect(Collectors.toList());
    }

    public void removeMedication(String id) {
        boolean removed = medications.removeIf(m -> m.getId().equals(id));

        if (removed) {
            saveToFile();
        }
    }

    public void validMedication(Medication med) {
        validateMedicationFields(med);

        if (findById(med.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "A medication with ID '" + med.getId() + "' already exists."
            );
        }
    }

    private void validateMedicationFields(Medication med) {
        if (med == null) {
            throw new IllegalArgumentException("Medication can't be null!");
        }
        if (med.getId() == null || med.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication ID is needed!");
        }
        if (med.getName() == null || med.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name is needed!");
        }
        if (med.getFrequencyPerDay() <= 0) {
            throw new IllegalArgumentException("Frequency value must be at least 1!");
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(medications);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Medication> loadFromFile() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Medication>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
