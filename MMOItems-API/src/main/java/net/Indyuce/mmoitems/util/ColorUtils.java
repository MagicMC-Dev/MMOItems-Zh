package net.Indyuce.mmoitems.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * mmoitems
 * 09/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ColorUtils {

    private static final Pattern STRIP_DECORATION_PATTERN = Pattern.compile("(?i)" + '§' + "[K-O]");

    public static @NotNull String stripDecoration(@NotNull String input) {
        return "%s%s".formatted(ChatColor.RESET, STRIP_DECORATION_PATTERN.matcher(input).replaceAll("")).replace('§', '&');
    }
}
