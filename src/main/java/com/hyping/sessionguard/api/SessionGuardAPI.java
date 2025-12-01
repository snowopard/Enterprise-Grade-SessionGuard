package com.hyping.sessionguard.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SessionGuardAPI {
    
    boolean hasActiveSession(@NotNull UUID playerId);
    
    @Nullable
    SessionData getSessionData(@NotNull UUID playerId);
    
    @NotNull
    CompletableFuture<Boolean> handleDuplicateLogin(
        @NotNull UUID playerId, 
        @NotNull String username
    );
    
    @NotNull
    CompletableFuture<Boolean> kickSession(@NotNull Player player, @NotNull String reason);
    
    int getActiveSessionCount();
    
    interface SessionData {
        @NotNull UUID getPlayerId();
        @NotNull String getUsername();
        long getLoginTime();
        long getLastActivity();
    }
}