package com.healthmedtracker.services;

import com.healthmedtracker.models.Medication;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class MedicationService {

    private final List<Medication> medications = new ArrayList<>();

    public void addMedication(Medication med) {
        medications.add(med);
    }

    public List<Medication> getAllMedications() {
        return medications;

    }

    public Optional<Medication> findById(String id) {
        return medications.stream().filter(m -> m.getId().equals(id)).findFirst();

    }

    public void removeMedication(String id) {
        medications.removeIf(m -> m.getId().equals(id));
    }
}
