package com.jakisniekot.playerFunctions.listener;

import com.jakisniekot.playerFunctions.Main;
import com.jakisniekot.playerFunctions.manager.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private Main plugin;
    private VanishManager manager;

    public PlayerEvents(
            Main plugin
    ) {
        this.plugin = plugin;
        this.manager = plugin.vanishManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //FINAL VALUES
        Player joined = event.getPlayer();
        Map<String, String> placeholder = new HashMap<>();

        //VANISH
        if (manager.wasVanished(joined) || joined.hasPermission("playerfunctions.vanish.auto")) {
            manager.setVanished(joined, true);
        }

        //MESSAGE
        if (!plugin.authme()) {
            if (!event.getPlayer().hasPlayedBefore()) {
                long total = Arrays.stream(Bukkit.getOfflinePlayers())
                        .count();
                String playerName = event.getPlayer().getName();

                placeholder.put("%allTimePlayerCount%", String.valueOf(total));
                placeholder.put("%player%", playerName);

                event.joinMessage(plugin.message("firstTimeJoinMessage", placeholder));

            } else {
                placeholder.put("%player%", event.getPlayer().getName());
                event.joinMessage(plugin.message("joinMessage", placeholder));
            }
        } else {
            if (!joined.hasPlayedBefore()) {
                plugin.authMeEvents().firstJoin.add(joined.getUniqueId());
            }
        }


        //VANISH
        for (UUID id : manager.getVanished()) {
            Player v = Bukkit.getPlayer(id);
            if (v != null && !joined.hasPermission("playerfunctions.vanish.see")) {
                joined.hidePlayer(plugin, v);
            }
        }

        if (plugin.authme()) {
            event.joinMessage(null);
        } else {
            if (plugin.vanishManager().isVanished(joined)) {
                event.joinMessage(null);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (manager.isVanished(event.getPlayer())) {
            event.quitMessage(null);
            return;
        } else {
            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("%player%", event.getPlayer().getName());
            event.quitMessage(plugin.message("quitMessage", placeholder));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        DamageSource src = e.getDamageSource();
        String type = src.getDamageType().getKey().getKey();
        Entity causing = src.getCausingEntity();

        if (causing instanceof Player) type = "player_attack";

        Map<String, String> plh = new HashMap<>();
        Map<String, Component> rich = new HashMap<>();
        plh.put("%victim%", victim.getName());

        if (causing instanceof Player killer) {
            plh.put("%killer%", killer.getName());
            rich.put("%item%", itemComponent(killer.getInventory().getItemInMainHand()));
        } else if (causing != null) {
            plh.put("%mob%", PlainTextComponentSerializer.plainText().serialize(causing.name()));
        }

        String path = plugin.getConfig().contains("deathMessages." + type)
                ? "deathMessages." + type
                : "deathMessages.default";

        if (!manager.isVanished(victim)) {
            e.deathMessage(plugin.message(path, plh, rich));
        } else {
            e.deathMessage(null);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p && manager.isVanished(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && manager.isVanished(p)) {
            e.setCancelled(true);
        }
    }

    private Component itemComponent(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return plugin.message("killedWithNothing");
        }
        return item.effectiveName().hoverEvent(item.asHoverEvent());
    }
}
