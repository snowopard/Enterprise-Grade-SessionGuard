package com.hyping.sessionguard.api;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SessionGuardAPI {

    boolean hasActiveSession(UUID playerId);

    CompletableFuture<Boolean> handleDuplicateLogin(UUID playerId, String username);

    CompletableFuture<Boolean> kickSession(Player player, String reason);

    int getActiveSessionCount();

    interface SessionData {
        UUID getPlayerId();
        String getUsername();
        long getLoginTime();
        long getLastActivity();
    }
}