package com.jakisniekot.playerFunctions.command;

import com.jakisniekot.playerFunctions.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IgnoreCommand implements CommandExecutor, TabCompleter {
    private Main plugin;
    public IgnoreCommand(
            Main plugin
    ) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("playerfunctions.ignore")) {
            plugin.playerMessage(player, "noPermission");
            return true;
        }

        Player target;
        String type = "add";

        Map<String, String> pl = new HashMap<>();

        if (args.length == 1) {
            pl.put("%player%", args[0]);
            target = Bukkit.getPlayer(args[0]);
        } else if (args.length == 2) {
            pl.put("%player%", args[1]);
            target = Bukkit.getPlayer(args[1]);
            type = args[0];
        } else {
            return true;
        }


        if (target == null) {
            plugin.playerMessage(player, "targetNotOnline", pl);
            return true;
        }

        if (!(type.equals("add") || type.equals("remove"))) {
            plugin.playerMessage(player, "ignoreUsageError");
            return true;
        }



        switch (type) {
            case "add":
                plugin.messageManager().addToIgnores(player, target.getUniqueId());
                plugin.playerMessage(player, "ignoreAddSuccess", pl);
                break;
            case "remove":
                plugin.messageManager().removeFromIgnores(player, target.getUniqueId());
                plugin.playerMessage(player, "ignoreRemoveSuccess", pl);
                break;
        }



        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (!player.hasPermission("playerfunctions.ignore")) {
            return List.of();
        }

        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                    break;
                case "remove":
                    completions.addAll(plugin.messageManager().getIgnored(player.getUniqueId()));
                    break;
            }
        }

        String lastWord = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastWord));

        return completions;
    }
}
