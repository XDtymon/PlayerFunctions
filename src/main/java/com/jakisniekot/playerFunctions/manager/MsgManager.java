package com.jakisniekot.playerFunctions.manager;

import com.jakisniekot.playerFunctions.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MsgManager {

    private Main plugin;

    private static final Map<UUID, UUID> replyLatest = new HashMap<>();

    public MsgManager(Main plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(String message, UUID senderUUID, UUID targetUUID) {
        Player sender = Bukkit.getPlayer(senderUUID);
        Player target = Bukkit.getPlayer(targetUUID);
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("%sender%", sender.getName());
        placeholder.put("%target%", target.getName());
        placeholder.put("%message%", message);

        plugin.playerMessage(sender, "msgUse", placeholder);
        if (!plugin.messageManager().isIgnoring(targetUUID, senderUUID)) plugin.playerMessage(target, "msgUse", placeholder);

        replyLatest.put(targetUUID, senderUUID);
    }

    public void sendReply(String message, UUID senderUUID) {
        UUID targetUUID = null;
        try {
            targetUUID = replyLatest.get(senderUUID);
        } catch (NullPointerException e) {

        }

        if (targetUUID != null) {
            sendMessage(message, senderUUID, targetUUID);
        } else {
            Player sender = Bukkit.getPlayer(senderUUID);
            plugin.playerMessage(sender, "noOneToReplyError");
        }
    }
}
