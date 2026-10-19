package com.jakisniekot.playerFunctions.listener;

import com.jakisniekot.playerFunctions.Main;
import fr.xephi.authme.settings.properties.DatabaseSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import fr.xephi.authme.events.LoginEvent;

import java.sql.*;
import java.util.*;

public class AuthMeEvents implements Listener {

    private Main plugin;

    public final Set<UUID> firstJoin = new HashSet<>();

    private final String sciezka;

    public AuthMeEvents(
            Main main
    ) {
        this.plugin = main;
        this.sciezka = plugin.getConfig().getString("authMePath");
    }

    @EventHandler
    public void onAuthLogin(LoginEvent event) {
        Player p = event.getPlayer();
        if (plugin.vanishManager().isVanished(p)) {
            firstJoin.remove(p.getUniqueId());
            return;
        }

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("%player%", p.getName());

        if (firstJoin.remove(p.getUniqueId())) {
            long total = getTotalPlayers();
            placeholder.put("%allTimePlayerCount%", String.valueOf(total));
            Bukkit.broadcast(plugin.message("firstTimeJoinMessage", placeholder));
        } else {
            Bukkit.broadcast(plugin.message("joinMessage", placeholder));
        }
    }

    public long getTotalPlayers() {
        try {
            return connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 350;
    }

    public long connect() throws SQLException {
        String url = "jdbc:sqlite:" + sciezka;
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM authme")) {
            return rs.getLong(1);
        }
    }
}
