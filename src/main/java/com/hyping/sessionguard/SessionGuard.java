package com.hyping.sessionguard;

import com.hyping.sessionguard.command.CommandManager;
import com.hyping.sessionguard.config.ConfigurationManager;
import com.hyping.sessionguard.listener.ConnectionListener;
import com.hyping.sessionguard.listener.PlayerActivityListener;
import com.hyping.sessionguard.manager.SessionManager;
import com.hyping.sessionguard.metrics.MetricsManager;
import com.hyping.sessionguard.api.SessionGuardAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class SessionGuard extends JavaPlugin {

    private static SessionGuard instance;
    private ConfigurationManager configurationManager;
    private SessionManager sessionManager;
    private CommandManager commandManager;
    private MetricsManager metricsManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Initialize configuration
            configurationManager = new ConfigurationManager(this);
            configurationManager.loadConfigurations();

            // Initialize manager
            sessionManager = new SessionManager(this);

            // Initialize metrics (optional - don't fail if it doesn't work)
            try {
                metricsManager = new MetricsManager(this);
            } catch (Exception e) {
                getLogger().warning("Could not initialize metrics: " + e.getMessage());
                // Continue without metrics
            }

            // Register API
            Bukkit.getServicesManager().register(
                    SessionGuardAPI.class,
                    sessionManager,
                    this,
                    ServicePriority.Normal
            );

            // Register listeners
            getServer().getPluginManager().registerEvents(
                    new ConnectionListener(this, sessionManager),
                    this
            );

            getServer().getPluginManager().registerEvents(
                    new PlayerActivityListener(sessionManager),
                    this
            );

            // Register commands
            commandManager = new CommandManager(this);
            commandManager.registerCommands();

            // Start cleanup task
            sessionManager.startCleanupTask();

            getLogger().info("SessionGuard v" + getDescription().getVersion() + " enabled!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable SessionGuard: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) {
            sessionManager.shutdown();
        }

        if (metricsManager != null) {
            metricsManager.shutdown();
        }

        getLogger().info("SessionGuard disabled gracefully");
    }

    public static SessionGuard getInstance() {
        return instance;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}