package com.hyping.sessionguard.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Pattern;

public class Validator {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );
    
    private Validator() {
        // Utility class
    }
    
    /**
     * Validate a Minecraft username
     */
    public static boolean isValidUsername(@Nullable String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate UUID format
     */
    public static boolean isValidUUID(@Nullable String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Validate IP address format
     */
    public static boolean isValidIPAddress(@Nullable String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // Check IPv4
        if (IPV4_PATTERN.matcher(ip).matches()) {
            return true;
        }
        
        // Check IPv6
        if (IPV6_PATTERN.matcher(ip).matches()) {
            return true;
        }
        
        // Try to parse as InetAddress
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    /**
     * Validate port number
     */
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }
    
    /**
     * Validate that an object is not null
     */
    public static <T> T requireNonNull(@Nullable T obj, @NotNull String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
    
    /**
     * Validate that a string is not null or empty
     */
    public static @NotNull String requireNonEmpty(@Nullable String str, @NotNull String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return str.trim();
    }
    
    /**
     * Validate that a number is within range
     */
    public static int requireInRange(int value, int min, int max, @NotNull String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    /**
     * Validate that a number is positive
     */
    public static long requirePositive(long value, @NotNull String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    /**
     * Validate configuration value
     */
    public static <T> T validateConfig(@Nullable T value, @NotNull T defaultValue, @NotNull String path) {
        if (value == null) {
            LoggerUtil.warning("Config value at '" + path + "' is null, using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
    
    /**
     * Sanitize string for logging
     */
    public static @NotNull String sanitizeForLog(@Nullable String input) {
        if (input == null) {
            return "null";
        }
        // Remove newlines and excessive whitespace
        return input.replaceAll("[\\r\\n]", " ").trim();
    }
}