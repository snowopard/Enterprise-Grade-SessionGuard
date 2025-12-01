package com.hyping.sessionguard.manager;

import com.hyping.sessionguard.SessionGuard;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class KickManager {
    
    private final SessionGuard plugin;
    
    public KickManager(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
    }
    
    public @NotNull CompletableFuture<Boolean> kickPlayer(
            @NotNull Player player, 
            @NotNull Component message,
            @NotNull String reason) {
        
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        player.getScheduler().run(plugin, task -> {
            try {
                player.kick(message);
                future.complete(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to kick player " + player.getName() + ": " + e.getMessage());
                future.complete(false);
            }
        }, null);
        
        return future;
    }
    
    public void shutdown() {
        // Cleanup resources if needed
    }
}