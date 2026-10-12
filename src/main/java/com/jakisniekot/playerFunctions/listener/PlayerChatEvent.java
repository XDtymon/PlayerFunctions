package com.jakisniekot.playerFunctions.listener;

import com.jakisniekot.playerFunctions.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

public class PlayerChatEvent implements Listener {

    private Main plugin;

    private String defaultPlayerString = "{prefix} &f{player} &7: &f{message}";

    /**
     * Ranga i jej defaultowa wiadomość
     */
    private Map<Permission, MessageType> playerMessages = new HashMap<>();

    public PlayerChatEvent(
            Main plugin
    ) {
        this.plugin = plugin;
        this.defaultPlayerString = plugin.getConfig().getString(
                "playerChat.default",
                "{prefix} &f{player} &7: &f{message}"
        );

        ConfigurationSection chatSection = plugin.getConfig().getConfigurationSection("playerChat");
        if (chatSection == null) {

        } else {
            for (String sectionID : chatSection.getKeys(false)) {


                Permission permission = new Permission("playerfunctions."+sectionID);
                MessageType messageType = new MessageType(

                );
            }
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {

        event.message();
    }
}
