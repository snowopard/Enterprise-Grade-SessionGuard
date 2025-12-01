package com.hyping.sessionguard.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SessionKickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final UUID playerId;
    private final String username;
    private final String reason;

    public SessionKickEvent(UUID playerId, String username, String reason) {
        super(true);
        this.playerId = playerId;
        this.username = username;
        this.reason = reason;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}