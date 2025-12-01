package com.hyping.sessionguard.manager;

import com.hyping.sessionguard.api.SessionGuardAPI.SessionData;
import com.hyping.sessionguard.util.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    
    private final Map<UUID, SessionData> activeSessions;
    private final Map<UUID, Long> activityTimestamps;
    private final Map<UUID, Long> kickTimestamps;
    private final Map<String, UUID> usernameToUUID;
    
    public CacheManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.activityTimestamps = new ConcurrentHashMap<>();
        this.kickTimestamps = new ConcurrentHashMap<>();
        this.usernameToUUID = new ConcurrentHashMap<>();
    }
    
    public boolean hasActiveSession(@NotNull UUID playerId) {
        return activeSessions.containsKey(playerId);
    }
    
    @Nullable
    public SessionData getSessionData(@NotNull UUID playerId) {
        return activeSessions.get(playerId);
    }
    
    public void addSession(@NotNull SessionData sessionData) {
        UUID playerId = sessionData.getPlayerId();
        String username = sessionData.getUsername();
        
        activeSessions.put(playerId, sessionData);
        activityTimestamps.put(playerId, System.currentTimeMillis());
        usernameToUUID.put(username.toLowerCase(), playerId);
        
        LoggerUtil.debug("Added session to cache: " + username + " (" + playerId + ")");
    }
    
    public void updateActivity(@NotNull UUID playerId) {
        activityTimestamps.put(playerId, System.currentTimeMillis());
    }
    
    public void removeSession(@NotNull UUID playerId) {
        SessionData sessionData = activeSessions.remove(playerId);
        if (sessionData != null) {
            activityTimestamps.remove(playerId);
            usernameToUUID.remove(sessionData.getUsername().toLowerCase());
            LoggerUtil.debug("Removed session from cache: " + sessionData.getUsername() + " (" + playerId + ")");
        }
    }
    
    @Nullable
    public UUID getPlayerIdByUsername(@NotNull String username) {
        return usernameToUUID.get(username.toLowerCase());
    }
    
    public void recordKick(@NotNull UUID playerId) {
        kickTimestamps.put(playerId, System.currentTimeMillis());
    }
    
    public boolean isRecentlyKicked(@NotNull UUID playerId, long delaySeconds) {
        Long kickTime = kickTimestamps.get(playerId);
        if (kickTime == null) {
            return false;
        }
        
        long delayMillis = TimeUnit.SECONDS.toMillis(delaySeconds);
        return System.currentTimeMillis() - kickTime < delayMillis;
    }
    
    public void cleanupExpired(long timeoutMinutes) {
        long timeoutMillis = TimeUnit.MINUTES.toMillis(timeoutMinutes);
        long currentTime = System.currentTimeMillis();
        
        // Clean active sessions
        activeSessions.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            Long lastActivity = activityTimestamps.get(playerId);
            if (lastActivity == null) {
                return true;
            }
            
            boolean expired = currentTime - lastActivity > timeoutMillis;
            if (expired) {
                activityTimestamps.remove(playerId);
                SessionData sessionData = entry.getValue();
                if (sessionData != null) {
                    usernameToUUID.remove(sessionData.getUsername().toLowerCase());
                }
                LoggerUtil.debug("Cleaned expired session: " + playerId);
            }
            return expired;
        });
        
        // Clean old kick timestamps (keep only last 5 minutes)
        kickTimestamps.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > TimeUnit.MINUTES.toMillis(5)
        );
    }
    
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    public void shutdown() {
        activeSessions.clear();
        activityTimestamps.clear();
        kickTimestamps.clear();
        usernameToUUID.clear();
        LoggerUtil.info("Cache manager shutdown complete");
    }
}