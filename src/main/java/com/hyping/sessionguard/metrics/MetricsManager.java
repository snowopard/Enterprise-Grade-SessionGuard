package com.hyping.sessionguard.metrics;

import com.hyping.sessionguard.SessionGuard;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class MetricsManager {

    private final SessionGuard plugin;
    private Metrics metrics;

    public MetricsManager(SessionGuard plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            // bStats plugin ID - you can get one from https://bstats.org/
            int pluginId = 12345; // Replace with your actual plugin ID

            metrics = new Metrics(plugin, pluginId);
            setupCharts();

            plugin.getLogger().info("Metrics initialized with bStats");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize metrics: " + e.getMessage());
            // Continue without metrics - it's not critical
        }
    }

    private void setupCharts() {
        if (metrics == null) return;

        // Storage type chart
        metrics.addCustomChart(new SimplePie("storage_type", () -> {
            return "MEMORY"; // Default storage type
        }));

        // Session count chart
        metrics.addCustomChart(new SingleLineChart("active_sessions", () -> {
            return plugin.getSessionManager().getActiveSessionCount();
        }));

        // Reconnection delay chart
        metrics.addCustomChart(new SimplePie("reconnection_delay", () -> {
            long delay = plugin.getConfig().getLong("reconnection.delay", 2L);
            return delay + "s";
        }));

        // Feature usage chart
        metrics.addCustomChart(new SimplePie("username_checking", () -> {
            boolean enabled = plugin.getConfig().getBoolean("detection.check-username", true);
            return enabled ? "Enabled" : "Disabled";
        }));
    }

    public void shutdown() {
        // bStats handles its own shutdown
        if (metrics != null) {
            plugin.getLogger().info("Metrics manager shutdown");
        }
    }

    public boolean isEnabled() {
        return metrics != null;
    }
}