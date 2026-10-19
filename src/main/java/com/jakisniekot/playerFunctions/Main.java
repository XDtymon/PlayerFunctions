package com.jakisniekot.playerFunctions;

import com.jakisniekot.playerFunctions.command.IgnoreCommand;
import com.jakisniekot.playerFunctions.command.MainCommand;
import com.jakisniekot.playerFunctions.command.MsgCommand;
import com.jakisniekot.playerFunctions.command.VanishCommand;
import com.jakisniekot.playerFunctions.listener.AuthMeEvents;
import com.jakisniekot.playerFunctions.listener.PlayerEvents;
import com.jakisniekot.playerFunctions.manager.MessageManager;
import com.jakisniekot.playerFunctions.manager.VanishManager;
import com.jakisniekot.playerFunctions.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private Permission permissions;
    private Chat chat;

    private Main plugin;

    private boolean vault;
    private boolean authme;
    private boolean luckperms;

    private MessageManager messageManager;
    private VanishManager vanishManager;

    private AuthMeEvents authMeEvents;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        saveDefaultConfig();

        vault = getServer().getPluginManager().getPlugin("Vault") != null;
        luckperms = getServer().getPluginManager().getPlugin("Luckperms") != null;
        authme = getServer().getPluginManager().getPlugin("AuthMe") != null;

        if (vault) {
            if (luckperms) permissions = getProvider(Permission.class);
            chat = getProvider(Chat.class);
        }

        if (authme) {
            authMeEvents = new AuthMeEvents(this);
            pm.registerEvents(authMeEvents, this);
        }

        if (!vault) getLogger().log(Level.CONFIG, "Vault not found");
        if (!luckperms) getLogger().log(Level.CONFIG, "Luckperms not found");
        if (!authme) getLogger().log(Level.CONFIG, "AuthMe not found");

        vanishManager = new VanishManager(this);
        messageManager = new MessageManager(this);

        pm.registerEvents(messageManager, this);
        pm.registerEvents(new PlayerEvents(this), this);

        MsgCommand msgCommand = new MsgCommand(this);
        VanishCommand vanishCommand = new VanishCommand(this);
        MainCommand mainCommand = new MainCommand(this);

        getCommand("ignore").setExecutor(new IgnoreCommand(this));
        getCommand("ignore").setTabCompleter(new IgnoreCommand(this));

        getCommand("message").setExecutor(msgCommand);
        getCommand("message").setTabCompleter(msgCommand);
        getCommand("reply").setExecutor(msgCommand);
        getCommand("reply").setTabCompleter(msgCommand);

        getCommand("vanish").setExecutor(vanishCommand);

        getCommand("playerfunctions").setExecutor(mainCommand);
        getCommand("playerfunctions").setTabCompleter(msgCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {

        saveDefaultConfig();
        reloadConfig();

        this.messageManager = new MessageManager(this);
    }

    private <T> T getProvider(Class<T> clazz) {
        RegisteredServiceProvider<T> rsp = getServer().getServicesManager().getRegistration(clazz);
        return rsp != null ? rsp.getProvider() : null;
    }

    public VanishManager vanishManager() {
        return vanishManager;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public AuthMeEvents authMeEvents() {
        return authMeEvents;
    }

    public boolean luckperms() {
        return luckperms;
    }

    public boolean vault() {
        return vault;
    }

    public boolean authme() {
        return authme;
    }

    public Chat chat() {
        return chat;
    }

    public Permission permissions() {
        return permissions;
    }

    /**
     *
     * @param message - accepts messageID and normal messages
     * @return - Colorful & Managed message component
     */
    public Component message(String message) {
        String textFromConfig = getConfig().getString(message);
        if (textFromConfig == null) return ColorUtil.color(message);
        return ColorUtil.color(textFromConfig);
    }

    public Component message(String message, Map<String, String> placeholder) {
        return message(message, placeholder, Map.of());
    }

    public Component message(String message,
                             Map<String, String> placeholder,
                             Map<String, Component> components) {

        String text = getConfig().getString(message);
        if (text == null) text = message;   // brak klucza w configu -> traktuj jako gotowy tekst

        // 1. zwykłe placeholdery tekstowe
        for (Map.Entry<String, String> entry : placeholder.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }

        // 2. kolory/formatowanie
        Component result = ColorUtil.color(text);

        // 3. placeholdery komponentowe (np. item z hoverem)
        for (Map.Entry<String, Component> entry : components.entrySet()) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(entry.getKey())
                    .replacement(entry.getValue())
                    .build());
        }

        return result;
    }

    public void playerMessage(Player player, String string) {
        player.sendMessage(message(string));
    }

    public void playerMessage(Player player, String string, Map<String, String> placeholder) {
        player.sendMessage(message(string, placeholder));
    }

    public void playerActionbar(Player player, String s) {
        player.sendActionBar(message(s));
    }

    public void playerActionbar(Player player, String s, Map<String , String> placeholder) {
        player.sendActionBar(message(s, placeholder));
    }

    public void broadcast(String s) {
        Bukkit.broadcast(message(s));
    }

    public void broadcast(String s, Map<String, String> placeholder) {
        Bukkit.broadcast(message(s, placeholder));
    }
}
