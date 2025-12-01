package com.hyping.sessionguard.storage.impl;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.api.SessionGuardAPI.SessionData;
import com.hyping.sessionguard.storage.SessionStorage;
import com.hyping.sessionguard.util.LoggerUtil;
import com.hyping.sessionguard.util.TimeUtil;
import com.hyping.sessionguard.util.UUIDUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class YamlStorage implements SessionStorage {
    
    private final SessionGuard plugin;
    private final File dataFile;
    private final ConcurrentHashMap<UUID, SessionData> cache;
    private FileConfiguration dataConfig;
    
    public YamlStorage(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "storage/sessions.yml");
        this.cache = new ConcurrentHashMap<>();
    }
    
    @Override
    public void initialize() {
        loadFromFile();
        
        // Auto-save task
        long autoSaveInterval = plugin.getConfig().getLong("storage.auto-save", 300L) * 20L;
        if (autoSaveInterval > 0) {
            plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, task -> saveToFile(), autoSaveInterval, autoSaveInterval);
        }
    }
    
    @Override
    public void shutdown() {
        saveToFile();
        cache.clear();
    }
    
    private void loadFromFile() {
        long startTime = System.currentTimeMillis();
        
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
            
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            cache.clear();
            
            ConfigurationSection sessionsSection = dataConfig.getConfigurationSection("sessions");
            if (sessionsSection != null) {
                for (String uuidStr : sessionsSection.getKeys(false)) {
                    UUID uuid = UUIDUtil.parseUUID(uuidStr);
                    if (uuid == null) {
                        continue;
                    }
                    
                    ConfigurationSection sessionSection = sessionsSection.getConfigurationSection(uuidStr);
                    if (sessionSection != null) {
                        SessionData sessionData = createSessionData(uuid, sessionSection);
                        cache.put(uuid, sessionData);
                    }
                }
            }
            
            LoggerUtil.info("Loaded " + cache.size() + " sessions from YAML storage");
            //LoggerUtil.logPerformance("YamlStorage.loadFromFile", startTime);
            
        } catch (IOException e) {
            LoggerUtil.error("Failed to load sessions from YAML", e);
        }
    }
    
    private void saveToFile() {
        long startTime = System.currentTimeMillis();
        
        try {
            if (dataConfig == null) {
                dataConfig = new YamlConfiguration();
            }
            
            dataConfig.set("sessions", null); // Clear existing
            
            ConfigurationSection sessionsSection = dataConfig.createSection("sessions");
            for (SessionData sessionData : cache.values()) {
                String uuidStr = sessionData.getPlayerId().toString();
                ConfigurationSection sessionSection = sessionsSection.createSection(uuidStr);
                
                sessionSection.set("username", sessionData.getUsername());
                sessionSection.set("login-time", sessionData.getLoginTime());
                sessionSection.set("last-activity", sessionData.getLastActivity());
            }
            
            dataConfig.save(dataFile);
            //LoggerUtil.logPerformance("YamlStorage.saveToFile", startTime);
            
        } catch (IOException e) {
            LoggerUtil.error("Failed to save sessions to YAML", e);
        }
    }
    
    private SessionData createSessionData(@NotNull UUID uuid, @NotNull ConfigurationSection section) {
        return new SessionData() {
            @Override
            public @NotNull UUID getPlayerId() {
                return uuid;
            }
            
            @Override
            public @NotNull String getUsername() {
                return section.getString("username", "Unknown");
            }
            
            @Override
            public long getLoginTime() {
                return section.getLong("login-time", System.currentTimeMillis());
            }
            
            @Override
            public long getLastActivity() {
                return section.getLong("last-activity", System.currentTimeMillis());
            }
        };
    }
    
    @Override
    public void saveSession(@NotNull SessionData sessionData) {
        cache.put(sessionData.getPlayerId(), sessionData);
    }
    
    @Override
    public @Nullable SessionData getSessionData(@NotNull UUID playerId) {
        return cache.get(playerId);
    }
    
    @Override
    public boolean hasActiveSession(@NotNull UUID playerId) {
        return cache.containsKey(playerId);
    }
    
    @Override
    public void updateActivity(@NotNull UUID playerId) {
        SessionData existing = cache.get(playerId);
        if (existing != null) {
            // Create new session data with updated activity
            final long currentTime = System.currentTimeMillis();
            SessionData updated = new SessionData() {
                @Override
                public @NotNull UUID getPlayerId() {
                    return existing.getPlayerId();
                }
                
                @Override
                public @NotNull String getUsername() {
                    return existing.getUsername();
                }
                
                @Override
                public long getLoginTime() {
                    return existing.getLoginTime();
                }
                
                @Override
                public long getLastActivity() {
                    return currentTime;
                }
            };
            cache.put(playerId, updated);
        }
    }
    
    @Override
    public void removeSession(@NotNull UUID playerId) {
        cache.remove(playerId);
    }
    
    @Override
    public void cleanupExpiredSessions(long timeout) {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> {
            long lastActivity = entry.getValue().getLastActivity();
            return currentTime - lastActivity > timeout;
        });
    }
    
    @Override
    public int getSessionCount() {
        return cache.size();
    }
}