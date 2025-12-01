package com.hyping.sessionguard.scheduler;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.manager.SessionManager;
import com.hyping.sessionguard.util.LoggerUtil;
import com.hyping.sessionguard.util.TimeUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

public class CleanupScheduler {
    
    private final SessionGuard plugin;
    private final SessionManager sessionManager;
    private ScheduledTask cleanupTask;
    
    public CleanupScheduler(@NotNull SessionGuard plugin, @NotNull SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }
    
    public void start() {
        long cleanupInterval = plugin.getConfig().getLong("performance.cleanup-interval", 60L);
        long intervalTicks = cleanupInterval * 20L; // Convert seconds to ticks
        
        cleanupTask = plugin.getServer().getGlobalRegionScheduler()
            .runAtFixedRate(plugin, task -> {
                performCleanup();
            }, 100L, intervalTicks); // Start after 5 seconds
        
        LoggerUtil.info("Cleanup scheduler started (interval: " + cleanupInterval + "s)");
    }
    
    public void stop() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
            cleanupTask = null;
            LoggerUtil.info("Cleanup scheduler stopped");
        }
    }
    
    private void performCleanup() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Cleanup expired sessions
            long timeoutMinutes = plugin.getConfig().getLong("session.timeout-minutes", 30L);
            sessionManager.shutdown(); // This triggers cleanup in storage
            
            LoggerUtil.debug("Cleanup completed in " + 
                TimeUtil.formatDuration(System.currentTimeMillis() - startTime));
            
        } catch (Exception e) {
            LoggerUtil.error("Error during cleanup", e);
        }
    }
    
    public boolean isRunning() {
        return cleanupTask != null && !cleanupTask.isCancelled();
    }
}