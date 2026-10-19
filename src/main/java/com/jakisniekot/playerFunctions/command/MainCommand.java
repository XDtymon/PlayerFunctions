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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    private Main plugin;
    public MainCommand(
            Main main
    ) {
        this.plugin = main;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("playerfunctions.admin")) {
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    plugin.reload();
                    plugin.playerMessage(player, "reload");
                    break;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        if (!player.hasPermission("playerfunctions.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
        }

        String lastWord = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastWord));

        return completions;
    }
}
