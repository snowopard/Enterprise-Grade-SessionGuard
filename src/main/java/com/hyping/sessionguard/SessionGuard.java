package com.hyping.sessionguard;

import com.hyping.sessionguard.api.SessionGuardAPI;
import com.hyping.sessionguard.command.CommandManager;
import com.hyping.sessionguard.config.ConfigurationManager;
import com.hyping.sessionguard.listener.ConnectionListener;
import com.hyping.sessionguard.listener.PlayerActivityListener;
import com.hyping.sessionguard.manager.SessionManager;
import com.hyping.sessionguard.storage.SessionStorage;
import com.hyping.sessionguard.storage.impl.MemoryStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class SessionGuard extends JavaPlugin {
    
    private static SessionGuard instance;
    private ConfigurationManager configurationManager;
    private SessionManager sessionManager;
    private SessionStorage sessionStorage;
    private CommandManager commandManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        try {

            com.hyping.sessionguard.util.LoggerUtil.initialize(this);
            com.hyping.sessionguard.util.Validator.validateConfig(
                getConfig(), "config.yml", "Configuration"
            );

            // Initialize configuration
            configurationManager = new ConfigurationManager(this);
            configurationManager.loadConfigurations();
            
            // Initialize storage
            sessionStorage = new MemoryStorage();
            sessionStorage.initialize();
            
            // Initialize managers
            sessionManager = new SessionManager(this, sessionStorage);
            commandManager = new CommandManager(this);
            
            // Register API
            Bukkit.getServicesManager().register(
                SessionGuardAPI.class,
                sessionManager,
                this,
                ServicePriority.Normal
            );
            
            // Register listeners
            Bukkit.getPluginManager().registerEvents(
                new ConnectionListener(this, sessionManager), 
                this
            );
            
            Bukkit.getPluginManager().registerEvents(
                new PlayerActivityListener(sessionManager), 
                this
            );
            
            // Register commands
            commandManager.registerCommands();
            
            // Start cleanup task
            sessionManager.startCleanupTask();
            
            getLogger().info("SessionGuard v" + getDescription().getVersion() + " enabled!");
            getLogger().info("Active sessions: " + sessionManager.getActiveSessionCount());
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable SessionGuard", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
        
        if (sessionStorage != null) {
            sessionStorage.shutdown();
        }
        
        getLogger().info("SessionGuard disabled gracefully");
    }
    
    public static SessionGuard getInstance() {
        return instance;
    }
    
    public @NotNull ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    public @NotNull SessionManager getSessionManager() {
        return sessionManager;
    }
}