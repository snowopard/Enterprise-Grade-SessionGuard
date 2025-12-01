package com.hyping.sessionguard.storage;

import com.hyping.sessionguard.api.SessionGuardAPI.SessionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface SessionStorage {
    
    void initialize();
    
    void shutdown();
    
    void saveSession(@NotNull SessionData sessionData);
    
    @Nullable
    SessionData getSessionData(@NotNull UUID playerId);
    
    boolean hasActiveSession(@NotNull UUID playerId);
    
    void updateActivity(@NotNull UUID playerId);
    
    void removeSession(@NotNull UUID playerId);
    
    void cleanupExpiredSessions(long timeout);
    
    int getSessionCount();
}