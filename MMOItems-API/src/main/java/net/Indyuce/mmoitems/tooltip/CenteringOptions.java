package net.Indyuce.mmoitems.tooltip;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CenteringOptions {

    private final boolean displayName;
    private final int[] span, fontSize;
    private final int loreLines;

    // Char settings
    private final Map<Character, Integer> charSizeExceptions = new HashMap<>();
    private final Pattern regex;

    public CenteringOptions(@NotNull ConfigurationSection config) {
        this.displayName = config.getBoolean("display_name");
        this.fontSize = intArray(Objects.requireNonNull(config.get("default_char_size"), "Could not find font size"));
        this.span = intArray(Objects.requireNonNull(config.get("span"), "Could not find span"));
        this.loreLines = config.getInt("lore_lines");
        this.regex = Pattern.compile(Objects.requireNonNull(config.getString("regex"), "Could not find char pattern"));

        if (config.contains("char_size")) {
            List<String> list = config.getStringList("char_size");
            for (String charSize : list) {
                char character = charSize.charAt(0);
                int size = Integer.parseInt(charSize.substring(1));
                charSizeExceptions.put(character, size);
            }
        }

        Validate.isTrue(displayName || loreLines > 0, "必须为至少一行说明文本或显示名称启用居中功能。");

        // Lore validation
        for (double d : fontSize) Validate.isTrue(d > 0, "字体大小必须为正");
        for (int i : span) Validate.isTrue(i > 0, "跨度必须是正的");
    }

    public boolean displayName() {
        return displayName;
    }

    public int getLoreLines() {
        return loreLines;
    }

    @NotNull
    public String centerLore(int j, String line) {
        return center(line, fontSize(1 + j), span(1 + j));
    }

    @NotNull
    public String centerName(@NotNull String line) {
        return center(line, fontSize(0), span(0));
    }

    @NotNull
    private String center(@NotNull String line, int fontSize, int span) {
        final int length = (int) Math.round(lengthApprox(line, fontSize)); // Approximate line length
        if (length >= span) return line; // Cannot center as it's too big
        final int offset = (span - length) / 2; // Either ceil or floor, not really important
        return UtilityMethods.getSpaceFont(offset) + line;
    }

    /**
     * Size in between two characters. I don't see how this is not
     * a constant in any font.
     */
    private static final double SEPARATOR_SIZE = 1;

    /**
     * Linear state machine that tries to approximate the length of a string
     * inside a tooltip. This is needed to then compute how many spaces are
     * required to center the item name at the middle of the lore tooltip.
     *
     * @param input String input
     * @return Approximate length of string input
     */
    private double lengthApprox(@NotNull String input, int fontSize) {

        double length = 0;
        boolean _isSpace = true;
        boolean _isColor = false;
        boolean _notEmpty = false;
        for (char next : input.toCharArray()) {

            // Ignore consecutive spaces
            final boolean isSpace = next == ' ';
            if (_isSpace && isSpace) continue;
            _isSpace = isSpace;

            // Ignore color codes
            // TODO change that when handling display name using components
            final boolean isColor = next == '§';
            if (_isColor && !isColor) {
                _isColor = false;
                continue;
            }
            _isColor = isColor;

            // Ignore non matching characters
            if (!this.regex.matcher(String.valueOf(next)).matches()) continue;

            if (_notEmpty) length += SEPARATOR_SIZE;
            length += charSize(next, fontSize);
            _notEmpty = true;
        }
        return length;
    }

    private int charSize(char character, int defaultFontSize) {
        @Nullable Integer found = charSizeExceptions.get(character);
        return found != null ? found : defaultFontSize;
    }

    private int span(int index) {
        return this.span[Math.min(index, this.span.length - 1)];
    }

    private int fontSize(int index) {
        return this.fontSize[Math.min(index, this.fontSize.length - 1)];
    }

    //region Reading from config

    private int[] intArray(Object obj) {
        if (obj instanceof Number) {
            return new int[]{((Number) obj).intValue()};
        }
        if (obj instanceof List) {
            List cast = (List) obj;
            int[] arr = new int[cast.size()];
            for (int i = 0; i < arr.length; i++)
                arr[i] = ((Number) cast.get(i)).intValue();
            return arr;
        }
        throw new RuntimeException("Expecting either an integer or integer list");
    }

    @Deprecated
    private String[] stringArray(Object obj) {
        if (obj instanceof String)
            return new String[]{(String) obj};

        if (obj instanceof List) {
            List cast = (List) obj;
            String[] arr = new String[cast.size()];
            for (int i = 0; i < arr.length; i++)
                arr[i] = String.valueOf(cast.get(i));
            return arr;
        }
        throw new RuntimeException("Expecting either a string or string list");
    }

    //endregion
}
