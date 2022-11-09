package net.Indyuce.mmoitems.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mmoitems
 * 09/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ColorUtils {

    private static final Pattern STRIP_DECORATION_PATTERN = Pattern.compile("(?i)" + '§' + "[K-O]");
    private static final Pattern COLOR_TAG_PATTERN = Pattern.compile("(?i)<.*>");
    private static final Pattern START_COLOR_TAG_PATTERN = Pattern.compile("(?i)<[^/]*>");
    private static final Pattern MINI_MSG_DECORATION_PATTERN = Pattern.compile("(?i)(<|</)(bold|italic|underlined|strikethrough|obfuscated|b|em|i|u|st|obf).*>");

    public static @NotNull String stripDecoration(@NotNull String input) {
        return "%s%s".formatted(ChatColor.RESET, MINI_MSG_DECORATION_PATTERN.matcher(STRIP_DECORATION_PATTERN.matcher(input).replaceAll("")).replaceAll(""))
                .replace('§', '&');
    }

    public static @NotNull String stripColors(@NotNull String input) {
        return ChatColor.stripColor(COLOR_TAG_PATTERN.matcher(input).replaceAll(""));
    }

    public static @NotNull String getLastColors(@NotNull String input) {
        Matcher matcher = START_COLOR_TAG_PATTERN.matcher(input);
        String lastMatch = null;
        while (matcher.find())
            lastMatch = matcher.group();
        return lastMatch == null ? ChatColor.getLastColors(input) : lastMatch;
    }
}
