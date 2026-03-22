package com.healthmedtracker.services;

import com.healthmedtracker.models.*;
import java.time.*;
import java.util.*;

public class ScheduleService {
    public List<ScheduledDose> generateDailySchedule(List<Medication> meds, LocalDate date){
        List <ScheduledDose> doses = new ArrayList<>();

        for (Medication med : meds){
            List<LocalTime> times = med.getDoseSchedule();
            if(times == null || times.isEmpty()) continue;
            for(LocalTime time : times){
                LocalDateTime scheduLocalDateTime = LocalDateTime.of(date, time);
                doses.add(new ScheduledDose(med, scheduLocalDateTime));
            } 
        }
        doses.sort(Comparator.comparing(ScheduledDose::getTime));

        return doses;

    }
}
