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
    
    private Validator() {
        // Utility class
    }
    
    public static boolean isValidUsername(@Nullable String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
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
    
    public static boolean isValidIPAddress(@Nullable String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // Check IPv4
        if (IPV4_PATTERN.matcher(ip).matches()) {
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
    
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }
    
    public static <T> T requireNonNull(@Nullable T obj, @NotNull String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
    
    public static @NotNull String requireNonEmpty(@Nullable String str, @NotNull String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return str.trim();
    }
    
    public static int requireInRange(int value, int min, int max, @NotNull String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    public static long requirePositive(long value, @NotNull String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    public static @NotNull String sanitizeForLog(@Nullable String input) {
        if (input == null) {
            return "null";
        }
        return input.replaceAll("[\\r\\n]", " ").trim();
    }
}