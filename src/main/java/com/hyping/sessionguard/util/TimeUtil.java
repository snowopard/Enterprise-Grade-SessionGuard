package com.hyping.sessionguard.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    
    public static long secondsToMillis(long seconds) {
        return TimeUnit.SECONDS.toMillis(seconds);
    }
    
    public static long minutesToMillis(long minutes) {
        return TimeUnit.MINUTES.toMillis(minutes);
    }
    
    public static @NotNull String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes < 60) {
            return minutes + "m " + seconds + "s";
        }
        
        long hours = minutes / 60;
        minutes %= 60;
        return hours + "h " + minutes + "m " + seconds + "s";
    }
}