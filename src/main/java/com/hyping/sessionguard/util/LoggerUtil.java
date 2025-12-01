package com.hyping.sessionguard.util;

import com.hyping.sessionguard.SessionGuard;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    
    private static Logger logger;
    private static SessionGuard plugin;
    
    private LoggerUtil() {
        // Utility class
    }
    
    public static void initialize(@NotNull SessionGuard pluginInstance) {
        plugin = pluginInstance;
        logger = pluginInstance.getLogger();
    }
    
    public static void info(@NotNull String message) {
        if (logger != null) {
            logger.info(message);
        } else {
            Bukkit.getLogger().info("[SessionGuard] " + message);
        }
    }
    
    public static void warning(@NotNull String message) {
        if (logger != null) {
            logger.warning(message);
        } else {
            Bukkit.getLogger().warning("[SessionGuard] " + message);
        }
    }
    
    public static void severe(@NotNull String message) {
        if (logger != null) {
            logger.severe(message);
        } else {
            Bukkit.getLogger().severe("[SessionGuard] " + message);
        }
    }
    
    public static void debug(@NotNull String message) {
        if (logger != null && isDebugEnabled()) {
            logger.info("[DEBUG] " + message);
        }
    }
    
    public static void error(@NotNull String message, @NotNull Throwable throwable) {
        severe(message + ": " + throwable.getMessage());
        if (isDebugEnabled()) {
            throwable.printStackTrace();
        }
    }
    
    public static void logDuplicateDetection(@NotNull String username, @NotNull String uuid, @NotNull String ip) {
        if (isLoggingEnabled("duplicates")) {
            info(String.format("Duplicate detected - User: %s, UUID: %s, IP: %s", 
                username, uuid, ip));
        }
    }
    
    public static void logKick(@NotNull String kickedPlayer, @NotNull String newPlayer, @NotNull String reason) {
        if (isLoggingEnabled("kicks")) {
            info(String.format("Kicked %s for %s by %s", 
                kickedPlayer, reason, newPlayer));
        }
    }
    
    public static void logReconnection(@NotNull String username, @NotNull String uuid) {
        if (isLoggingEnabled("reconnections")) {
            info(String.format("Reconnection allowed for %s (UUID: %s)", 
                username, uuid));
        }
    }
    
    private static boolean isDebugEnabled() {
        return plugin != null && plugin.getConfig().getBoolean("logging.debug", false);
    }
    
    private static boolean isLoggingEnabled(@NotNull String type) {
        if (plugin == null) return true;
        return plugin.getConfig().getBoolean("logging.enabled", true) &&
               plugin.getConfig().getBoolean("logging." + type, true);
    }
    
    public static void logPerformance(@NotNull String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 100 && isDebugEnabled()) { // Log if operation took >100ms
            warning(String.format("Performance warning: %s took %dms", operation, duration));
        }
    }
}