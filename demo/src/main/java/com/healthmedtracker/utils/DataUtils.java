package com.healthmedtracker.utils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataUtils {
    public static String today() { 
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")); }
    
}
