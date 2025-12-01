package com.hyping.sessionguard.manager;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.api.SessionGuardAPI;
import com.hyping.sessionguard.api.event.SessionKickEvent;
import com.hyping.sessionguard.storage.SessionStorage;
import com.hyping.sessionguard.storage.impl.MemoryStorage;
import com.hyping.sessionguard.util.ComponentUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SessionManager implements SessionGuardAPI {

    private final SessionGuard plugin;
    private final SessionStorage storage;
    private final Map<UUID, Long> recentlyKicked;
    private final Map<UUID, Boolean> processingKicks;
    private ScheduledTask cleanupTask;

    public SessionManager(SessionGuard plugin) {
        this.plugin = plugin;
        this.storage = new MemoryStorage();
        this.storage.initialize();
        this.recentlyKicked = new ConcurrentHashMap<>();
        this.processingKicks = new ConcurrentHashMap<>();
    }

    public void startCleanupTask() {
        cleanupTask = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, task -> {
                    cleanupExpiredData();
                }, 100L, 20L * 10L);
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
    public boolean hasActiveSession(UUID playerId) {
        return storage.hasActiveSession(playerId);
    }

    @Override
    public CompletableFuture<Boolean> handleDuplicateLogin(UUID playerId, String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Check if already processing
        if (processingKicks.containsKey(playerId)) {
            future.complete(false);
            return future;
        }

        // Check reconnection window
        Long kickTime = recentlyKicked.get(playerId);
        long reconnectDelay = plugin.getConfig().getLong("reconnection.delay", 2L) * 1000L;

        if (kickTime != null && System.currentTimeMillis() - kickTime < reconnectDelay) {
            recentlyKicked.remove(playerId);
            future.complete(true);
            return future;
        }

        // Find existing player - fix: make variables effectively final
        final Player existingPlayer = findExistingPlayer(playerId, username);

        if (existingPlayer == null || !existingPlayer.isOnline()) {
            future.complete(true);
            return future;
        }

        // Mark as processing
        processingKicks.put(playerId, true);

        // Get kick message - make it effectively final
        final String kickMessageStr = plugin.getConfig().getString("kick.duplicate",
                "&cYou logged in from another location!");
        final Component kickMessage = ComponentUtil.parseLegacy(kickMessageStr);
        final String existingPlayerName = existingPlayer.getName(); // Capture as final

        // Create atomic reference for future completion
        final AtomicBoolean operationCompleted = new AtomicBoolean(false);

        // Perform kick
        existingPlayer.getScheduler().run(plugin, task -> {
            try {
                existingPlayer.kick(kickMessage);
                recentlyKicked.put(playerId, System.currentTimeMillis());

                // Fire kick event
                Bukkit.getPluginManager().callEvent(
                        new SessionKickEvent(playerId, username, "DUPLICATE_LOGIN")
                );

                plugin.getLogger().info("Kicked " + existingPlayerName +
                        " for duplicate login by " + username);

                future.complete(true);
                operationCompleted.set(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to kick player: " + e.getMessage());
                future.complete(false);
            } finally {
                if (operationCompleted.get()) {
                    processingKicks.remove(playerId);
                }
            }
        }, null);

        return future;
    }

    private Player findExistingPlayer(UUID playerId, String username) {
        Player existingPlayer = Bukkit.getPlayer(playerId);
        if (existingPlayer == null && plugin.getConfig().getBoolean("detection.check-username", true)) {
            existingPlayer = Bukkit.getPlayerExact(username);
        }
        return existingPlayer;
    }

    @Override
    public CompletableFuture<Boolean> kickSession(Player player, String reason) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Make variables effectively final
        final String kickMessageStr = plugin.getConfig().getString("kick.admin",
                "&cYour session was terminated by an administrator.");
        final Component kickMessage = ComponentUtil.parseLegacy(kickMessageStr);
        final UUID playerId = player.getUniqueId();
        final String playerName = player.getName(); // Capture as final

        player.getScheduler().run(plugin, task -> {
            try {
                player.kick(kickMessage);
                removeSession(playerId);
                plugin.getLogger().info("Kicked player " + playerName + " for reason: " + reason);
                future.complete(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to kick player: " + e.getMessage());
                future.complete(false);
            }
        }, null);

        return future;
    }

    public void createSession(Player player) {
        UUID playerId = player.getUniqueId();
        String username = player.getName();
        long currentTime = System.currentTimeMillis();

        SessionGuardAPI.SessionData sessionData = new SessionGuardAPI.SessionData() {
            @Override
            public UUID getPlayerId() {
                return playerId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public long getLoginTime() {
                return currentTime;
            }

            @Override
            public long getLastActivity() {
                return currentTime;
            }
        };

        storage.saveSession(sessionData);
        recentlyKicked.remove(playerId);
    }

    public void updateActivity(UUID playerId) {
        storage.updateActivity(playerId);
    }

    public void removeSession(UUID playerId) {
        storage.removeSession(playerId);
    }

    @Override
    public int getActiveSessionCount() {
        return storage.getSessionCount();
    }

    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        storage.shutdown();
    }
}