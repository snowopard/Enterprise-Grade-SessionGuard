package com.hyping.sessionguard.config.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Messages, String> messages;
    
    public MessageManager() {
        this.messages = new HashMap<>();
    }
    
    public void load(@NotNull FileConfiguration config) {
        for (Messages message : Messages.values()) {
            String path = message.getPath();
            String value = config.getString(path, message.getDefaultValue());
            messages.put(message, value);
        }
    }
    
    public @NotNull Component get(@NotNull Messages message, @NotNull TagResolver... resolvers) {
        String raw = messages.getOrDefault(message, message.getDefaultValue());
        return miniMessage.deserialize(raw, resolvers);
    }
    
    public @NotNull String getRaw(@NotNull Messages message) {
        return messages.getOrDefault(message, message.getDefaultValue());
    }
    
    public enum Messages {
        KICK_DUPLICATE("kick.duplicate", 
            "<red>You logged in from another location!"),
        
        KICK_ADMIN("kick.admin", 
            "<red>Your session was terminated by an administrator."),
        
        PLAYER_WELCOME("player.welcome",
            "<green>Welcome! Your session is secure.");
        
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