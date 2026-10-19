package com.jakisniekot.playerFunctions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Pattern HEX_PATTERN =
            Pattern.compile("(?i)&?#([A-F0-9]{6})");

    private static final Pattern LEGACY_PATTERN =
            Pattern.compile("(?i)&([0-9A-FK-OR])");

    private ColorUtil() {
    }

    public static Component color(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Hex najpierw, żeby &#ff0000 nie zostało pomylone z kodem legacy
        text = replaceHexColors(text);
        text = replaceLegacyColors(text);

        return MINI_MESSAGE.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static String colorLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String replacement = ChatColor.of("#" + matcher.group(1)).toString();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);

        // Klient Minecrafta sam resetuje formatowanie po kodzie koloru
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String replaceHexColors(String text) {
        // Kolor resetuje poprzednie formatowanie (jak w legacy)
        return HEX_PATTERN.matcher(text).replaceAll("<reset><#$1>");
    }

    private static String replaceLegacyColors(String text) {
        Matcher matcher = LEGACY_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(toTag(code)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String toTag(char code) {
        return switch (code) {
            // kolory: reset formatowania + kolor
            case '0' -> "<reset><black>";
            case '1' -> "<reset><dark_blue>";
            case '2' -> "<reset><dark_green>";
            case '3' -> "<reset><dark_aqua>";
            case '4' -> "<reset><dark_red>";
            case '5' -> "<reset><dark_purple>";
            case '6' -> "<reset><gold>";
            case '7' -> "<reset><gray>";
            case '8' -> "<reset><dark_gray>";
            case '9' -> "<reset><blue>";
            case 'a' -> "<reset><green>";
            case 'b' -> "<reset><aqua>";
            case 'c' -> "<reset><red>";
            case 'd' -> "<reset><light_purple>";
            case 'e' -> "<reset><yellow>";
            case 'f' -> "<reset><white>";
            // formatowanie: bez resetu, można łączyć &l&n
            case 'l' -> "<bold>";
            case 'o' -> "<italic>";
            case 'n' -> "<underlined>";
            case 'm' -> "<strikethrough>";
            case 'k' -> "<obfuscated>";
            case 'r' -> "<reset>";
            default -> "";
        };
    }
}