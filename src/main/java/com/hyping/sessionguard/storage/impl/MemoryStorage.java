package com.hyping.sessionguard.storage.impl;

import com.hyping.sessionguard.api.SessionGuardAPI.SessionData;
import com.hyping.sessionguard.storage.SessionStorage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage implements SessionStorage {

    private final Map<UUID, SessionData> sessions;
    private final Map<UUID, Long> activityTimes;

    public MemoryStorage() {
        this.sessions = new ConcurrentHashMap<>();
        this.activityTimes = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        // Nothing to initialize
    }

    @Override
    public void shutdown() {
        sessions.clear();
        activityTimes.clear();
    }

    @Override
    public void saveSession(SessionData sessionData) {
        UUID playerId = sessionData.getPlayerId();
        sessions.put(playerId, sessionData);
        activityTimes.put(playerId, System.currentTimeMillis());
    }

    @Override
    public SessionData getSessionData(UUID playerId) {
        return sessions.get(playerId);
    }

    @Override
    public boolean hasActiveSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    @Override
    public void updateActivity(UUID playerId) {
        activityTimes.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void removeSession(UUID playerId) {
        sessions.remove(playerId);
        activityTimes.remove(playerId);
    }

    @Override
    public void cleanupExpiredSessions(long timeout) {
        long currentTime = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            Long lastActivity = activityTimes.get(entry.getKey());
            if (lastActivity == null) {
                return true;
            }
            return currentTime - lastActivity > timeout;
        });

        activityTimes.entrySet().removeIf(entry -> !sessions.containsKey(entry.getKey()));
    }

    @Override
    public int getSessionCount() {
        return sessions.size();
    }
}