package com.healthmedtracker.models;
import java.time.LocalDateTime;
public class ScheduledDose {
    private final Medication medication;
    private final LocalDateTime time;
    private boolean taken;
    private boolean skipped;

    private boolean notified = false;

public boolean isNotified() { 
    return notified; 
}
public void markNotified() {
     this.notified = true;
     }
public ScheduledDose(Medication medication, LocalDateTime time){
    this.medication = medication;
    this.time = time;
}
public Medication getMedication() {
    return medication;
}
public LocalDateTime getTime(){
    return time;
}
public boolean isTaken(){
    return taken;
}
public boolean isSkipped(){
    return skipped;
}

public void markTaken(){
    this.taken = true;
    this.skipped = false;

}
public void markSkipped(){
    this.taken = false;
    this.skipped = true;
}
}