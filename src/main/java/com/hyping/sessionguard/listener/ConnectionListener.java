package com.hyping.sessionguard.listener;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.api.event.SessionDuplicateEvent;
import com.hyping.sessionguard.api.event.SessionReconnectEvent;
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

    public ConnectionListener(SessionGuard plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String username = event.getName();

        if (sessionManager.hasActiveSession(uuid)) {
            plugin.getLogger().info("Duplicate login detected for " + username + " (UUID: " + uuid + ")");

            // Fire duplicate event
            SessionDuplicateEvent duplicateEvent = new SessionDuplicateEvent(uuid, username);
            Bukkit.getPluginManager().callEvent(duplicateEvent);

            if (duplicateEvent.isCancelled()) {
                event.allow();
                return;
            }

            CompletableFuture<Boolean> future = sessionManager.handleDuplicateLogin(uuid, username);

            try {
                Boolean result = future.get(3, TimeUnit.SECONDS);

                if (result != null && result) {
                    event.allow();
                    plugin.getLogger().info("Allowed reconnection for " + username);

                    // Fire reconnect event
                    Bukkit.getPluginManager().callEvent(new SessionReconnectEvent(uuid, username));
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
        String welcomeMessage = plugin.getConfig().getString("player.welcome",
                "&aWelcome! Your session is now secure.");

        if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
            player.sendMessage(welcomeMessage.replace('&', '§'));
        }

        plugin.getLogger().info("Player " + player.getName() + " joined successfully");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessionManager.removeSession(player.getUniqueId());
    }
}