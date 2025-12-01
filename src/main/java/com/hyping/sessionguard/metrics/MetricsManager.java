package com.hyping.sessionguard.metrics;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.util.LoggerUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.charts.AdvancedPie;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MetricsManager {
    
    private final SessionGuard plugin;
    private Metrics metrics;
    
    public MetricsManager(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
        
        if (plugin.getConfig().getBoolean("features.metrics", true)) {
            initialize();
        }
    }
    
    private void initialize() {
        try {
            int pluginId = 12345; // Replace with your bStats plugin ID
            
            metrics = new Metrics(plugin, pluginId);
            setupCharts();
            
            LoggerUtil.info("Metrics initialized with bStats");
            
        } catch (Exception e) {
            LoggerUtil.error("Failed to initialize metrics", e);
        }
    }
    
    private void setupCharts() {
        if (metrics == null) return;
        
        // Storage type chart
        metrics.addCustomChart(new SimplePie("storage_type", () -> {
            return plugin.getConfig().getString("storage.type", "MEMORY");
        }));
        
        // Session count chart
        metrics.addCustomChart(new SingleLineChart("active_sessions", () -> {
            return plugin.getSessionManager().getActiveSessionCount();
        }));
        
        // Feature usage chart
        metrics.addCustomChart(new AdvancedPie("feature_usage", () -> {
            Map<String, Integer> values = new HashMap<>();
            
            values.put("reconnection_enabled", 
                plugin.getConfig().getBoolean("reconnection.enabled", true) ? 1 : 0);
            values.put("ip_checking", 
                plugin.getConfig().getBoolean("detection.check-ip", true) ? 1 : 0);
            values.put("username_checking", 
                plugin.getConfig().getBoolean("detection.check-username", true) ? 1 : 0);
            values.put("logging_enabled", 
                plugin.getConfig().getBoolean("logging.enabled", true) ? 1 : 0);
            
            return values;
        }));
        
        // Reconnection delay chart
        metrics.addCustomChart(new SimplePie("reconnection_delay", () -> {
            long delay = plugin.getConfig().getLong("reconnection.delay", 2L);
            if (delay <= 2) return "2s_or_less";
            if (delay <= 5) return "3-5s";
            if (delay <= 10) return "6-10s";
            return "10s+";
        }));
    }
    
    public void shutdown() {
        // bStats handles its own shutdown
        LoggerUtil.info("Metrics manager shutdown");
    }
    
    public boolean isEnabled() {
        return metrics != null;
    }
}