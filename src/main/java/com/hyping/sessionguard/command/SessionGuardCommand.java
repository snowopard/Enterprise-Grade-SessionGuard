package com.hyping.sessionguard.command;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.manager.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SessionGuardCommand implements TabExecutor {
    
    private final SessionGuard plugin;
    private final SessionManager sessionManager;
    
    public SessionGuardCommand(@NotNull SessionGuard plugin) {
        this.plugin = plugin;
        this.sessionManager = plugin.getSessionManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "stats":
                return handleStats(sender);
            case "list":
                return handleList(sender);
            case "kick":
                return handleKick(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        plugin.getConfigurationManager().reloadAll();
        sender.sendMessage("§aConfiguration reloaded!");
        return true;
    }
    
    private boolean handleStats(@NotNull CommandSender sender) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        sender.sendMessage("§6§lSessionGuard Statistics");
        sender.sendMessage("§eActive sessions: §f" + sessionManager.getActiveSessionCount());
        return true;
    }
    
    private boolean handleList(@NotNull CommandSender sender) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        List<String> sessions = sessionManager.getAllActiveSessions();
        if (sessions.isEmpty()) {
            sender.sendMessage("§7No active sessions");
        } else {
            sender.sendMessage("§6§lActive Sessions (§f" + sessions.size() + "§6)");
            for (String session : sessions) {
                sender.sendMessage("§7- §e" + session);
            }
        }
        return true;
    }
    
    private boolean handleKick(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /sessionguard kick <player>");
            return true;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage("§cPlayer not found or offline!");
            return true;
        }
        
        sessionManager.kickSession(target, "Admin command")
            .thenAccept(success -> {
                if (success) {
                    sender.sendMessage("§aKicked " + target.getName() + "'s session");
                } else {
                    sender.sendMessage("§cFailed to kick " + target.getName());
                }
            });
        
        return true;
    }
    
    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage("§6§lSessionGuard Commands");
        if (sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§e/sessionguard reload §7- Reload configuration");
            sender.sendMessage("§e/sessionguard stats §7- View statistics");
            sender.sendMessage("§e/sessionguard list §7- List active sessions");
            sender.sendMessage("§e/sessionguard kick <player> §7- Kick player session");
        }
        sender.sendMessage("§e/sessionguard help §7- Show this help");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("help");
            if (sender.hasPermission("sessionguard.admin")) {
                completions.add("reload");
                completions.add("stats");
                completions.add("list");
                completions.add("kick");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}