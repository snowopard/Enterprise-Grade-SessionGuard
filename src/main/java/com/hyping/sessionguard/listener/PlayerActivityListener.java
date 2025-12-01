package com.hyping.sessionguard.listener;

import com.hyping.sessionguard.manager.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerActivityListener implements Listener {
    
    private final SessionManager sessionManager;
    
    public PlayerActivityListener(@NotNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        sessionManager.updateActivity(event.getPlayer().getUniqueId());
    }
}