package com.healthmedtracker.services;

import com.healthmedtracker.models.*;
import java.time.*;
import java.util.*;

public class ScheduleService {
    public List<ScheduledDose> generateDailySchedule(List<Medication> meds, LocalDate date){
        List <ScheduledDose> doses = new ArrayList<>();

        for (Medication med : meds){
            int freq = med.getfrequencyPerDay();
            if (freq <= 0) continue;

            LocalTime start = LocalTime.of(8,0);
            LocalTime end = LocalTime.of(20,0);

            long interval = (end.toSecondOfDay() - start.toSecondOfDay())/ (freq - 1 == 0 ? 1: (freq-1));
            for (int i=0; i < freq; i++){
                LocalTime doseTime = start.plusSeconds(interval * i);
                doses.add(new ScheduledDose(med, LocalDateTime.of(date,doseTime)));
                
            }
        }

        return doses;

    }
}
