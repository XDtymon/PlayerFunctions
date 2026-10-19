package com.jakisniekot.playerFunctions.command;

import com.jakisniekot.playerFunctions.Main;
import com.jakisniekot.playerFunctions.manager.MsgManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MsgCommand implements CommandExecutor, TabCompleter {
    private Main plugin;
    private MsgManager msgManager;

    private String msg = "&e%sender% &6» &e%target% &8>> &7%message%";

    public MsgCommand(
            Main main
    ) {
        this.plugin = main;
        if (plugin.getConfig().getString("msgUse") != null) {
            this.msg = plugin.getConfig().getString("msgUse");
        }
        this.msgManager = new MsgManager(plugin);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        switch (label) {
            case "msg", "message":
                if (!player.hasPermission("playerfunctions.message")) {
                    plugin.playerMessage(player, "noPermission");
                    return true;
                }

                if (args.length < 2) {
                    plugin.playerMessage(player, "msgUsageError");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    plugin.playerMessage(player, "targetNotOnline");
                    return true;
                }

                if (target.getUniqueId() == player.getUniqueId()) {
                    plugin.playerMessage(player, "selfTargetError");
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                msgManager.sendMessage(message, player.getUniqueId(), target.getUniqueId());
                break;
            case "r", "reply":
                if (!player.hasPermission("playerfunctions.reply")) {
                    plugin.playerMessage(player, "noPermission");
                }

                String reply = String.join(" ", args);

                msgManager.sendReply(reply, player.getUniqueId());
                break;
            case null, default:
                return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        switch (label) {
            case "msg", "message":
                if (!player.hasPermission("playerfunctions.message")) {
                    return List.of();
                }
                break;
            case "r", "reply":
                if (!player.hasPermission("playerfunctions.reply")) {
                    return List.of();
                }
                break;
            case null, default:
                return List.of();
        }



        List<String> completions = new ArrayList<>();

        switch (label) {
            case "msg", "message":
                if (args.length == 1) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                }
                break;
            case "reply", "r":
                break;
        }

        String lastWord = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastWord));

        return completions;
    }
}
