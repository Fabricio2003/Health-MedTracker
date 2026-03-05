package com.healthmedtracker.services;
import com.healthmedtracker.models.Medication;
import java.util.ArrayList;
 import java.util.List;

public class MedicationService {
    
    private final List<Medication>medications = new ArrayList<>(); 
    public void addMedication(Medication med) { medications.add(med); 
    } 
    public List<Medication> getAllMedications() {
         return medications;
    
    }
}
