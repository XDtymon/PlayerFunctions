package com.jakisniekot.playerFunctions.manager;

import com.jakisniekot.playerFunctions.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final Main plugin;
    private final Set<UUID> vanished = new HashSet<>();
    private final NamespacedKey vanishKey;

    public VanishManager(Main plugin) {
        this.plugin = plugin;
        this.vanishKey = new NamespacedKey(plugin, "vanished");
    }

    public boolean isVanished(Player p) {
        return vanished.contains(p.getUniqueId());
    }

    public Set<UUID> getVanished() {
        return Set.copyOf(vanished);
    }

    public boolean wasVanished(Player p) {
        Byte b = p.getPersistentDataContainer().get(vanishKey, PersistentDataType.BYTE);
        return b != null && b == 1;
    }

    public void setVanished(Player p, boolean value) {
        p.getPersistentDataContainer().set(vanishKey, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        if (value) {
            vanished.add(p.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(p) && !other.hasPermission("playerfunctions.vanish.see")) {
                    other.hidePlayer(plugin, p);
                }
            }
            p.setAffectsSpawning(false);
            p.setSleepingIgnored(true);
            plugin.playerActionbar(p, "vanishON");
        } else {
            vanished.remove(p.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, p);
            }
            p.setAffectsSpawning(true);
            p.setSleepingIgnored(false);
            plugin.playerActionbar(p, "vanishOFF");
        }
    }

    public NamespacedKey getVanishKey() {
        return vanishKey;
    }
}