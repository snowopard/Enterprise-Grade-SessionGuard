package com.hyping.sessionguard.manager;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.api.SessionGuardAPI;
import com.hyping.sessionguard.api.event.SessionDuplicateEvent;
import com.hyping.sessionguard.api.event.SessionKickEvent;
import com.hyping.sessionguard.api.event.SessionReconnectEvent;
import com.hyping.sessionguard.config.messages.MessageManager;
import com.hyping.sessionguard.storage.SessionStorage;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SessionManager implements SessionGuardAPI {
    
    private final SessionGuard plugin;
    private final SessionStorage storage;
    private final KickManager kickManager;
    private final CacheManager cacheManager;
    private final ReconnectionManager reconnectionManager;
    private ScheduledTask cleanupTask;
    
    public SessionManager(@NotNull SessionGuard plugin, @NotNull SessionStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.kickManager = new KickManager(plugin);
        this.cacheManager = new CacheManager();
        this.reconnectionManager = new ReconnectionManager(plugin);
    }
    
    public void startCleanupTask() {
        cleanupTask = Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(plugin, task -> {
                cleanupExpiredData();
            }, 100L, 20L * 10L); // Every 10 seconds
    }
    
    private void cleanupExpiredData() {
        long currentTime = System.currentTimeMillis();
        long reconnectDelay = plugin.getConfig().getLong("reconnection.delay", 2L) * 1000L;
        
        // Clean recently kicked players
        recentlyKicked.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > reconnectDelay);
        
        // Clean processing kicks
        processingKicks.clear();
        
        // Clean storage
        storage.cleanupExpiredSessions(TimeUnit.MINUTES.toMillis(
            plugin.getConfig().getLong("session.timeout-minutes", 30L)
        ));
    }
    
    @Override
    public boolean hasActiveSession(@NotNull UUID playerId) {
        return storage.hasActiveSession(playerId);
    }
    
    @Override
    public @Nullable SessionData getSessionData(@NotNull UUID playerId) {
        return storage.getSessionData(playerId);
    }
    
    @Override
    public @NotNull CompletableFuture<Boolean> handleDuplicateLogin(
            @NotNull UUID playerId, 
            @NotNull String username) {
        
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Check reconnection window using reconnectionManager
        if (reconnectionManager.canReconnect(playerId)) {
            future.complete(false);
            return future;
        }
        
        // Check reconnection window
        Long kickTime = recentlyKicked.get(playerId);
        long reconnectDelay = plugin.getConfig().getLong("reconnection.delay", 2L) * 1000L;
        
        if (kickTime != null && System.currentTimeMillis() - kickTime < reconnectDelay) {
            recentlyKicked.remove(playerId);
            Bukkit.getPluginManager().callEvent(new SessionReconnectEvent(playerId, username));
            future.complete(true);
            return future;
        }
        
        // Fire duplicate event
        SessionDuplicateEvent event = new SessionDuplicateEvent(playerId, username);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            future.complete(true);
            return future;
        }
        
        // Find existing player
        Player existingPlayer = Bukkit.getPlayer(playerId);
        if (existingPlayer == null && plugin.getConfig().getBoolean("detection.check-username", true)) {
            existingPlayer = Bukkit.getPlayerExact(username);
        }
        
        if (existingPlayer == null || !existingPlayer.isOnline()) {
            future.complete(true);
            return future;
        }
        
        // Mark as processing
        processingKicks.add(playerId);
        
        // Get kick message
        MessageManager.Messages kickMessageType = MessageManager.Messages.KICK_DUPLICATE;
        Component kickMessage = plugin.getConfigurationManager()
            .getMessageManager()
            .get(kickMessageType);
        
        // Perform kick
        kickManager.kickPlayer(existingPlayer, kickMessage, "DUPLICATE_LOGIN")
            .thenAccept(success -> {
                processingKicks.remove(playerId);
                
                if (success) {
                    recentlyKicked.put(playerId, System.currentTimeMillis());
                    Bukkit.getPluginManager().callEvent(
                        new SessionKickEvent(playerId, username, "DUPLICATE_LOGIN")
                    );
                    
                    plugin.getLogger().info("Kicked " + existingPlayer.getName() + 
                        " for duplicate login by " + username);
                    
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            })
            .exceptionally(throwable -> {
                processingKicks.remove(playerId);
                plugin.getLogger().severe("Failed to kick player: " + throwable.getMessage());
                future.complete(false);
                return null;
            });
        
        return future;
    }
    
    @Override
    public @NotNull CompletableFuture<Boolean> kickSession(@NotNull Player player, @NotNull String reason) {
        Component kickMessage = plugin.getConfigurationManager()
            .getMessageManager()
            .get(MessageManager.Messages.KICK_ADMIN);
        
        return kickManager.kickPlayer(player, kickMessage, reason);
    }
    
    public void createSession(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        String username = player.getName();
        long loginTime = System.currentTimeMillis();
        
        SessionData sessionData = new SessionData() {
            @Override
            public @NotNull UUID getPlayerId() {
                return playerId;
            }
            
            @Override
            public @NotNull String getUsername() {
                return username;
            }
            
            @Override
            public long getLoginTime() {
                return loginTime;
            }
            
            @Override
            public long getLastActivity() {
                return loginTime;
            }
        };
        
        storage.saveSession(sessionData);
        recentlyKicked.remove(playerId); // Clear from kicked list
    }
    
    public void updateActivity(@NotNull UUID playerId) {
        storage.updateActivity(playerId);
    }
    
    public void removeSession(@NotNull UUID playerId) {
        storage.removeSession(playerId);
    }
    
    @Override
    public int getActiveSessionCount() {
        return storage.getSessionCount();
    }
    
    public List<String> getAllActiveSessions() {
        List<String> sessions = new ArrayList<>();
        // This would be implemented based on storage backend
        return sessions;
    }
    
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        kickManager.shutdown();
    }
}