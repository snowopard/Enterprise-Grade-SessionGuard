package com.hyping.sessionguard.manager;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.util.LoggerUtil;
import com.hyping.sessionguard.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReconnectionManager {
    
    private final SessionGuard plugin;
    private final Map<UUID, ReconnectionData> reconnectionQueue;
    
    public ReconnectionManager(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
        this.reconnectionQueue = new ConcurrentHashMap<>();
    }
    
    /**
     * Record a player kick for reconnection tracking
     */
    public void recordKick(@NotNull UUID playerId, @NotNull String username, @NotNull String reason) {
        ReconnectionData data = new ReconnectionData(
            playerId, 
            username, 
            System.currentTimeMillis(),
            reason
        );
        
        reconnectionQueue.put(playerId, data);
        
        long delaySeconds = plugin.getConfig().getLong("reconnection.delay", 2L);
        LoggerUtil.info(String.format(
            "Player %s kicked. Reconnection allowed in %d seconds",
            username, delaySeconds
        ));
    }
    
    /**
     * Check if a player can reconnect
     */
    public boolean canReconnect(@NotNull UUID playerId) {
        ReconnectionData data = reconnectionQueue.get(playerId);
        if (data == null) {
            return true; // Not in queue, allow reconnection
        }
        
        long delaySeconds = plugin.getConfig().getLong("reconnection.delay", 2L);
        long delayMillis = TimeUnit.SECONDS.toMillis(delaySeconds);
        
        if (System.currentTimeMillis() - data.kickTime >= delayMillis) {
            // Reconnection window passed
            reconnectionQueue.remove(playerId);
            LoggerUtil.logReconnection(data.username, playerId.toString());
            return true;
        }
        
        // Still within delay window
        long remaining = delayMillis - (System.currentTimeMillis() - data.kickTime);
        LoggerUtil.debug(String.format(
            "Reconnection denied for %s. Remaining wait: %s",
            data.username, TimeUtil.formatDuration(remaining)
        ));
        
        return false;
    }
    
    /**
     * Force allow reconnection (admin command)
     */
    public boolean forceAllowReconnection(@NotNull UUID playerId) {
        if (reconnectionQueue.remove(playerId) != null) {
            LoggerUtil.info("Force allowed reconnection for " + playerId);
            return true;
        }
        return false;
    }
    
    /**
     * Get reconnection status
     */
    @NotNull
    public ReconnectionStatus getStatus(@NotNull UUID playerId) {
        ReconnectionData data = reconnectionQueue.get(playerId);
        if (data == null) {
            return new ReconnectionStatus(false, 0, null);
        }
        
        long delaySeconds = plugin.getConfig().getLong("reconnection.delay", 2L);
        long delayMillis = TimeUnit.SECONDS.toMillis(delaySeconds);
        long remaining = Math.max(0, delayMillis - (System.currentTimeMillis() - data.kickTime));
        
        return new ReconnectionStatus(true, remaining, data.reason);
    }
    
    /**
     * Cleanup old reconnection records
     */
    public void cleanup() {
        long cleanupThreshold = TimeUnit.MINUTES.toMillis(5); // Keep records for 5 minutes
        
        reconnectionQueue.entrySet().removeIf(entry -> {
            long age = System.currentTimeMillis() - entry.getValue().kickTime;
            return age > cleanupThreshold;
        });
    }
    
    public int getQueueSize() {
        return reconnectionQueue.size();
    }
    
    public void shutdown() {
        reconnectionQueue.clear();
        LoggerUtil.info("Reconnection manager shutdown");
    }
    
    // Data classes
    private static class ReconnectionData {
        final UUID playerId;
        final String username;
        final long kickTime;
        final String reason;
        
        ReconnectionData(UUID playerId, String username, long kickTime, String reason) {
            this.playerId = playerId;
            this.username = username;
            this.kickTime = kickTime;
            this.reason = reason;
        }
    }
    
    public static class ReconnectionStatus {
        public final boolean isInQueue;
        public final long remainingMillis;
        public final String reason;
        
        ReconnectionStatus(boolean isInQueue, long remainingMillis, String reason) {
            this.isInQueue = isInQueue;
            this.remainingMillis = remainingMillis;
            this.reason = reason;
        }
        
        public int getRemainingSeconds() {
            return (int) TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
        }
    }
}