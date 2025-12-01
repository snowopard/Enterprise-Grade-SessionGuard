package com.hyping.sessionguard.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SessionDuplicateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final UUID playerId;
    private final String username;
    private boolean cancelled;
    
    public SessionDuplicateEvent(@NotNull UUID playerId, @NotNull String username) {
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
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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