package com.hyping.sessionguard.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SessionDuplicateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final UUID playerId;
    private final String username;
    private boolean cancelled;

    public SessionDuplicateEvent(UUID playerId, String username) {
        super(true);
        this.playerId = playerId;
        this.username = username;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}