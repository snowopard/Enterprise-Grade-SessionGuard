package com.hyping.sessionguard.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SessionReconnectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final UUID playerId;
    private final String username;
    
    public SessionReconnectEvent(@NotNull UUID playerId, @NotNull String username) {
        super(true);
        this.playerId = playerId;
        this.username = username;
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
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}