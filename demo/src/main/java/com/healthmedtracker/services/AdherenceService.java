package com.healthmedtracker.services;
import com.healthmedtracker.models.ScheduledDose;
import java.time.LocalDate;
import java.util.List;

public class AdherenceService{
    public double calculateDailyAdherence(List<ScheduledDose> schedule, LocalDate date){
        long total = schedule.stream().filter(d-> d.getTime().toLocalDate().equals(date)).count();
        if (total ==0) return 1.0;
        long taken = schedule.stream().filter(d -> d.getTime().toLocalDate().equals(date)).filter(ScheduledDose::isTaken).count();
        return (double) taken/total;
    }

}