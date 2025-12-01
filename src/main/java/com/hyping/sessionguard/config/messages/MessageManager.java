package com.hyping.sessionguard.config.messages;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final Map<Messages, String> messages;
    
    public MessageManager() {
        this.messages = new HashMap<>();
    }
    
    public void load(FileConfiguration config) {
        for (Messages message : Messages.values()) {
            String path = message.getPath();
            String value = config.getString(path, message.getDefaultValue());
            messages.put(message, value);
        }
    }
    
    public String get(Messages message) {
        return messages.getOrDefault(message, message.getDefaultValue());
    }
    
    public enum Messages {
        KICK_DUPLICATE("kick.duplicate", "&cYou logged in from another location!"),
        KICK_ADMIN("kick.admin", "&cYour session was terminated by an administrator."),
        PLAYER_WELCOME("player.welcome", "&aWelcome! Your session is secure.");
        
        private final String path;
        private final String defaultValue;
        
        Messages(String path, String defaultValue) {
            this.path = path;
            this.defaultValue = defaultValue;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
    }
}