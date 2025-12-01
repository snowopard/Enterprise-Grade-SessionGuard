package com.hyping.sessionguard.command;

import com.hyping.sessionguard.SessionGuard;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public class CommandManager {
    
    private final SessionGuard plugin;
    private final SessionGuardCommand commandExecutor;
    
    public CommandManager(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
        this.commandExecutor = new SessionGuardCommand(plugin);
    }
    
    public void registerCommands() {
        PluginCommand command = plugin.getCommand("sessionguard");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }
    }
}