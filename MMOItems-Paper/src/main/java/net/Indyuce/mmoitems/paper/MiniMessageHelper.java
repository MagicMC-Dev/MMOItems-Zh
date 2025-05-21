package net.Indyuce.mmoitems.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class MiniMessageHelper {
    private static final Pattern HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§([0-9a-fk-or])");

    @NotNull
    @Deprecated
    public static Component deserialize(String input) {
        return MiniMessage.miniMessage().deserialize(convertToMiniMessage(input));
    }

    @NotNull
    @Deprecated
    public static String convertToMiniMessage(String input) {
        // Convert hex color codes (e.g., §x§3§1§B§2§C§B → <#31B2CB>)
        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        StringBuffer hexResult = new StringBuffer();

        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group();
            String miniMessageHex = convertHexToMiniMessage(hexCode);
            hexMatcher.appendReplacement(hexResult, miniMessageHex);
        }
        hexMatcher.appendTail(hexResult);
        input = hexResult.toString(); // Update input after hex replacement

        // Convert single-character legacy color codes (e.g., §a → <green>)
        Matcher matcher = COLOR_CODE_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = convertCodeToMiniMessage(code);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static String convertCodeToMiniMessage(String code) {
        //@formatter:off
        switch (code.toLowerCase()) {
            case "0": return "<black>";
            case "1": return "<dark_blue>";
            case "2": return "<dark_green>";
            case "3": return "<dark_aqua>";
            case "4": return "<dark_red>";
            case "5": return "<dark_purple>";
            case "6": return "<gold>";
            case "7": return "<gray>";
            case "8": return "<dark_gray>";
            case "9": return "<blue>";
            case "a": return "<green>";
            case "b": return "<aqua>";
            case "c": return "<red>";
            case "d": return "<light_purple>";
            case "e": return "<yellow>";
            case "f": return "<white>";
            case "k": return "<obfuscated>";
            case "l": return "<bold>";
            case "m": return "<strikethrough>";
            case "n": return "<underline>";
            case "o": return "<italic>";
            case "r": return "<reset>";
            default: return "";
        }
        //@formatter:on
    }

    private static String convertHexToMiniMessage(String hexCode) {
        // Extract hex values
        StringBuilder hexColor = new StringBuilder("#");
        for (int i = 2; i < hexCode.length(); i += 2) {
            hexColor.append(hexCode.charAt(i + 1)); // Extract the valid color hex characters
        }
        return "<" + hexColor.toString() + ">";
    }
}
