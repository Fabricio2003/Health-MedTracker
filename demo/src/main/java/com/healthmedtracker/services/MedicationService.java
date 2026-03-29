package com.healthmedtracker.services;

import com.healthmedtracker.models.Medication;

import java.util.*;
import java.util.stream.Collectors;



public class MedicationService {

    private final List<Medication> medications = new ArrayList<>();
    

    public void addMedication(Medication med) {
        validMedication(med); // validates the id, name, frequency
        medications.add(med);

    }

    public void updateMedication(String id, Medication updateMed){
        validMedication(updateMed);
        for(int i = 0; i < medications.size(); i++){
            if(medications.get(i).getId().equals(id)){
                medications.set(i, updateMed);
                return;
            }
        }
    }

    public List<Medication> getAllMedications() {
        return medications;

    }

    public Optional<Medication> findById(String id) {
        if(id == null)
            return Optional.empty();
        return medications.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public List<Medication> findByName(String name){
        if(name == null || name.trim().isEmpty())
            return new ArrayList<>();
        String lowerCaseName = name.toLowerCase();
        return medications.stream().filter(m -> m.getName().toLowerCase().contains(lowerCaseName)).collect(Collectors.toList()); 
    }

    public void removeMedication(String id) {
        medications.removeIf(m -> m.getId().equals(id));
    }


    public void validMedication(Medication med){
        if(med == null){
            throw new IllegalArgumentException("Medication can't be null!");
        }
        if(med.getId() == null || med.getId().trim().isEmpty()){
            throw new IllegalArgumentException("Medication ID is needed!");
        }
        if(med.getName() == null || med.getName().trim().isEmpty()){
            throw new  IllegalArgumentException("Medication name is needed!");
        }
        if(med.getfrequencyPerDay() < 0){
            throw new IllegalArgumentException("Frequency value can't be negative!");
        }
        if (findById(med.getId()).isPresent()) {
            throw new IllegalArgumentException("A medication with ID '" + med.getId() + "' already exists.");
        }
    }

    
}



