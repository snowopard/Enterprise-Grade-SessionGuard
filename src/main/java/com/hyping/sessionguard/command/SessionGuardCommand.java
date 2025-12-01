package com.hyping.sessionguard.command;

import com.hyping.sessionguard.SessionGuard;
import com.hyping.sessionguard.manager.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionGuardCommand implements TabExecutor {

    private final SessionGuard plugin;
    private final SessionManager sessionManager;

    public SessionGuardCommand(SessionGuard plugin) {
        this.plugin = plugin;
        this.sessionManager = plugin.getSessionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "stats":
                return handleStats(sender);
            case "kick":
                return handleKick(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        plugin.getConfigurationManager().reloadAll();
        sender.sendMessage("§aConfiguration reloaded!");
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        sender.sendMessage("§6§lSessionGuard Statistics");
        sender.sendMessage("§eActive sessions: §f" + sessionManager.getActiveSessionCount());
        return true;
    }

    private boolean handleKick(CommandSender sender, String[] args) {
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

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lSessionGuard Commands");
        if (sender.hasPermission("sessionguard.admin")) {
            sender.sendMessage("§e/sessionguard reload §7- Reload configuration");
            sender.sendMessage("§e/sessionguard stats §7- View statistics");
            sender.sendMessage("§e/sessionguard kick <player> §7- Kick player session");
        }
        sender.sendMessage("§e/sessionguard help §7- Show this help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            if (sender.hasPermission("sessionguard.admin")) {
                completions.add("reload");
                completions.add("stats");
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