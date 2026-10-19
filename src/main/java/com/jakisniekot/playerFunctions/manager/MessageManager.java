package com.jakisniekot.playerFunctions.manager;

import com.jakisniekot.playerFunctions.Main;
import com.jakisniekot.playerFunctions.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements Listener {

    private Main plugin;
    private Map<String, MessageType> playerMessages = new HashMap<>();
    private Map<UUID, Set<UUID>> ignores = new HashMap<>();

    private final NamespacedKey key;

    public MessageManager(
            Main plugin
    ) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "ignored");

        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            for (String group : plugin.permissions().getGroups()) {
                ConfigurationSection section =  plugin.getConfig()
                        .getConfigurationSection("playerChat")
                        .getConfigurationSection(group);
                if (section == null) continue;
                String format = section.getString("message");
                if (format == null) {
                    format = "&f{player} &7>> &f{message}";
                }

                MessageType messageType = new MessageType(format, section.getBoolean("colorMessage"));
                playerMessages.put(group, messageType);
            }

            if (playerMessages.isEmpty()) {
                playerMessages.put(
                        "default",
                        new MessageType(
                                "&f{player} &7>> &r{message}",
                                false
                        )
                );
            }
        } else {
            playerMessages.put(
                    "default",
                    new MessageType(
                            "&f{player} &7>> &r{message}",
                            false
                    )
            );
        }



    }

    private static final PersistentDataType<?, List<String>> LIST_TYPE =
            PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING);

    public Set<UUID> load(Player player) {
        List<String> raw = player.getPersistentDataContainer().get(key, LIST_TYPE);
        Set<UUID> result = ConcurrentHashMap.newKeySet();
        if (raw != null) {
            for (String s : raw) {
                try {
                    result.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return result;
    }

    public void save(Player player, Set<UUID> ignored) {
        List<String> raw = ignored.stream().map(UUID::toString).toList();
        if (raw.isEmpty()) {
            player.getPersistentDataContainer().remove(key);
        } else {
            player.getPersistentDataContainer().set(key, LIST_TYPE, raw);
        }
    }

    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> set = ignores.get(player);
        return set != null && set.contains(target);
    }

    public List<String> getIgnored(UUID player) {
        return ignores.get(player).stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .toList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ignores.put(p.getUniqueId(), load(p));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ignores.remove(e.getPlayer().getUniqueId());
        if (ignores.get(e.getPlayer().getUniqueId()) == null) return;
        save(e.getPlayer(), ignores.get(e.getPlayer().getUniqueId()));
    }

    public void addToIgnores(Player player, UUID target) {
        Set<UUID> set = ignores.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        set.add(target);
    }

    public void removeFromIgnores(Player player, UUID target) {
        Set<UUID> set = ignores.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        set.remove(target);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String prefix = "";
        if (plugin.vault() && plugin.luckperms()) {
            prefix = plugin.chat().getPlayerPrefix(event.getPlayer());

            if (prefix == null || prefix.isBlank()) {
                prefix = "";
            }

            String group = plugin.permissions().getPrimaryGroup(event.getPlayer());
            MessageType messageType = playerMessages.get(group);
            if (messageType == null) messageType = playerMessages.get("default");
            final String finalFormat = messageType.getMessage()
                    .replace("{player}", event.getPlayer().getName())
                    .replace("{prefix}", prefix);

            event.viewers().removeIf(audience ->
                    audience instanceof Player p
                            && !p.getUniqueId().equals(event.getPlayer().getUniqueId())
                            && isIgnoring(p.getUniqueId(), event.getPlayer().getUniqueId()));

            event.renderer((source, sourceDisplayName, message, viewer) -> {
                String messageText =
                        PlainTextComponentSerializer.plainText().serialize(message);
                String result = finalFormat.replace("{message}", messageText);
                return ColorUtil.color(result);
            });
        } else {
            final String finalFormat = playerMessages.get("default").getMessage()
                    .replace("{player}", event.getPlayer().getName())
                    .replace("{prefix}", prefix);

            event.viewers().removeIf(audience ->
                    audience instanceof Player p
                            && !p.getUniqueId().equals(event.getPlayer().getUniqueId())
                            && isIgnoring(p.getUniqueId(), event.getPlayer().getUniqueId()));

            event.renderer((source, sourceDisplayName, message, viewer) -> {
                String messageText = LegacyComponentSerializer.legacyAmpersand().serialize(message);
                String result = finalFormat.replace("{message}", messageText);
                return ColorUtil.color(result);
            });
        }
    }
}
