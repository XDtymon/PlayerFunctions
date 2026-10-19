package com.jakisniekot.playerFunctions.command;

import com.jakisniekot.playerFunctions.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class VanishCommand implements CommandExecutor {
    private final Main plugin;

    public VanishCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }
        if (!p.hasPermission("playerfunctions.vanish.use")) {
            plugin.playerMessage(p, "noPermission");
            return true;
        }

        boolean newState = !plugin.vanishManager().isVanished(p);

        plugin.vanishManager().setVanished(p, newState);

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("%player%", p.getName());

        if (newState) {
            plugin.broadcast("quitMessage", placeholder);
        } else {
            plugin.broadcast("joinMessage", placeholder);
        }

        return true;
    }
}