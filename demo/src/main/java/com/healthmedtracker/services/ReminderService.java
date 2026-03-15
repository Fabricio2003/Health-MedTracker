package com.healthmedtracker.services;
import com.healthmedtracker.models.ScheduledDose;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderService {
    public List<ScheduledDose> getUpcomingDoses(List<ScheduledDose> schedule, LocalDateTime now, int windowMinutes){
        LocalDateTime windowEnd = now.plusMinutes(windowMinutes);
        return schedule.stream().filter(d->!d.isTaken()&& !d.isSkipped()).filter(d -> !d.getTime().isBefore(now) && !d.getTime().isAfter(windowEnd))
                .collect(Collectors.toList());
        
    }
    public List<ScheduledDose> getOverdueDoses(List<ScheduledDose> schedule, LocalDateTime now){
        return schedule.stream().filter(d -> !d.isTaken() && !d.isSkipped()).filter(d -> d.getTime().isBefore(now)).collect(Collectors.toList());
    }
}
