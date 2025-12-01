package com.hyping.sessionguard.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtil {
    
    private UUIDUtil() {
        // Utility class
    }
    
    /**
     * Parse UUID from string, return null if invalid
     */
    @Nullable
    public static UUID parseUUID(@Nullable String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return null;
        }
        
        try {
            // Handle with and without dashes
            if (uuidString.length() == 32) {
                // No dashes
                return UUID.fromString(
                    uuidString.substring(0, 8) + "-" +
                    uuidString.substring(8, 12) + "-" +
                    uuidString.substring(12, 16) + "-" +
                    uuidString.substring(16, 20) + "-" +
                    uuidString.substring(20, 32)
                );
            } else if (uuidString.length() == 36) {
                // With dashes
                return UUID.fromString(uuidString);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        return null;
    }
    
    /**
     * Convert UUID to bytes
     */
    @NotNull
    public static byte[] toBytes(@NotNull UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
    
    /**
     * Convert bytes to UUID
     */
    @NotNull
    public static UUID fromBytes(@NotNull byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be 16 bytes long");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }
    
    /**
     * Convert UUID to string without dashes
     */
    @NotNull
    public static String toStringNoDashes(@NotNull UUID uuid) {
        return uuid.toString().replace("-", "");
    }
    
    /**
     * Generate a short UUID (8 characters)
     */
    @NotNull
    public static String toShortString(@NotNull UUID uuid) {
        return uuid.toString().substring(0, 8);
    }
    
    /**
     * Check if two UUIDs are equal (null-safe)
     */
    public static boolean equals(@Nullable UUID uuid1, @Nullable UUID uuid2) {
        if (uuid1 == null && uuid2 == null) {
            return true;
        }
        if (uuid1 == null || uuid2 == null) {
            return false;
        }
        return uuid1.equals(uuid2);
    }
    
    /**
     * Get hash code for UUID (null-safe)
     */
    public static int hashCode(@Nullable UUID uuid) {
        return uuid != null ? uuid.hashCode() : 0;
    }
}