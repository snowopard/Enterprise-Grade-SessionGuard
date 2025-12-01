package com.hyping.sessionguard.listener;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.api.SessionGuardAPI;
import com.hyping.sessionguard.manager.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConnectionListener implements Listener {
    
    private final SessionGuard plugin;
    private final SessionManager sessionManager;
    
    public ConnectionListener(@NotNull SessionGuard plugin, @NotNull SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String username = event.getName();
        
        if (sessionManager.hasActiveSession(uuid)) {
            plugin.getLogger().info("Duplicate login detected for " + username + " (UUID: " + uuid + ")");
            
            CompletableFuture<Boolean> future = sessionManager.handleDuplicateLogin(uuid, username);
            
            try {
                Boolean result = future.get(3, TimeUnit.SECONDS);
                
                if (result != null && result) {
                    event.allow();
                    plugin.getLogger().info("Allowed reconnection for " + username);
                } else {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                        "Could not establish connection");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Timeout handling duplicate login for " + username);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Connection timeout");
            }
        } else {
            event.allow();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        
        Player player = event.getPlayer();
        sessionManager.createSession(player);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String welcomeMessage = plugin.getConfigurationManager()
            .getMessageManager()
            .getRaw(com.hyping.sessionguard.config.messages.MessageManager.Messages.PLAYER_WELCOME);
        
        if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
            player.sendMessage(welcomeMessage);
        }
        
        plugin.getLogger().info("Player " + player.getName() + " joined successfully");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessionManager.removeSession(player.getUniqueId());
    }
}