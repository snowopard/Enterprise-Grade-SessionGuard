package com.hyping.sessionguard.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SessionKickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final UUID playerId;
    private final String username;
    private final String reason;
    
    public SessionKickEvent(@NotNull UUID playerId, @NotNull String username, @NotNull String reason) {
        super(true);
        this.playerId = playerId;
        this.username = username;
        this.reason = reason;
    }
    
    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }
    
    @NotNull
    public String getUsername() {
        return username;
    }
    
    @NotNull
    public String getReason() {
        return reason;
    }
    
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}