package com.hyping.sessionguard.scheduler;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.manager.ReconnectionManager;
import com.hyping.sessionguard.util.LoggerUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

public class ReconnectScheduler {
    
    private final SessionGuard plugin;
    private final ReconnectionManager reconnectionManager;
    private ScheduledTask reconnectTask;
    
    public ReconnectScheduler(@NotNull SessionGuard plugin, @NotNull ReconnectionManager reconnectionManager) {
        this.plugin = plugin;
        this.reconnectionManager = reconnectionManager;
    }
    
    public void start() {
        long checkInterval = 20L; // Check every second
        
        reconnectTask = plugin.getServer().getGlobalRegionScheduler()
            .runAtFixedRate(plugin, task -> {
                checkReconnections();
            }, 20L, checkInterval);
        
        LoggerUtil.info("Reconnection scheduler started");
    }
    
    public void stop() {
        if (reconnectTask != null && !reconnectTask.isCancelled()) {
            reconnectTask.cancel();
            reconnectTask = null;
            LoggerUtil.info("Reconnection scheduler stopped");
        }
    }
    
    private void checkReconnections() {
        try {
            // Cleanup old records
            reconnectionManager.cleanup();
            
            // Log queue size if debug enabled
            int queueSize = reconnectionManager.getQueueSize();
            if (queueSize > 0) {
                LoggerUtil.debug("Reconnection queue size: " + queueSize);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Error in reconnection scheduler", e);
        }
    }
    
    public boolean isRunning() {
        return reconnectTask != null && !reconnectTask.isCancelled();
    }
}