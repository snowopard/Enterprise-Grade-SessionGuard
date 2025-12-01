package com.hyping.sessionguard.config;

import com.hyping.sessionguard.config.messages.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigurationManager {
    
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configurations;
    private final MessageManager messageManager;
    
    public ConfigurationManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.configurations = new HashMap<>();
        this.messageManager = new MessageManager();
    }
    
    public void loadConfigurations() {
        loadConfiguration("config.yml");
        loadConfiguration("messages.yml");
        messageManager.load(getConfiguration("messages.yml"));
    }
    
    private void loadConfiguration(@NotNull String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream inputStream = plugin.getResource(fileName)) {
                if (inputStream != null) {
                    Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create config: " + fileName, e);
                return;
            }
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            configurations.put(fileName, config);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load config: " + fileName, e);
        }
    }
    
    public void reloadAll() {
        configurations.clear();
        loadConfigurations();
    }
    
    public @NotNull FileConfiguration getConfiguration(@NotNull String fileName) {
        return configurations.getOrDefault(fileName, plugin.getConfig());
    }
    
    public @NotNull FileConfiguration getConfig() {
        return getConfiguration("config.yml");
    }
    
    public @NotNull MessageManager getMessageManager() {
        return messageManager;
    }
}