package com.hyping.sessionguard.storage;

import com.hyping.sessionguard.api.SessionGuardAPI.SessionData;

import java.util.UUID;

public interface SessionStorage {

    void initialize();

    void shutdown();

    void saveSession(SessionData sessionData);

    SessionData getSessionData(UUID playerId);

    boolean hasActiveSession(UUID playerId);

    void updateActivity(UUID playerId);

    void removeSession(UUID playerId);

    void cleanupExpiredSessions(long timeout);

    int getSessionCount();
}