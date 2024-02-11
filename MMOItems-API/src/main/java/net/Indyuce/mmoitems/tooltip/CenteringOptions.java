package net.Indyuce.mmoitems.tooltip;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CenteringOptions {

    // Display name
    private final boolean name;
    private final double nameFontSize;
    private final int nameSpan;
    private final String namePrefix;

    // Lore
    private final int loreLines;
    private final List<Double> loreFontSize;
    private final List<Integer> loreSpan;

    private final Pattern regex;

    private static final String DEFAULT_REGEX = "(?i)[^&§][a-z][a-z ]*[a-z]";
    private static final double SEPARATOR_SPACE = 1;

    public CenteringOptions(@NotNull ConfigurationSection config) {

        this.name = config.getBoolean("display_name.enabled");
        this.nameFontSize = config.getDouble("display_name.font_size");
        this.nameSpan = config.getInt("display_name.span");
        this.namePrefix = config.getString("display_name.prefix", "");

        this.loreLines = config.getInt("lore.lines");
        this.loreFontSize = config.getDoubleList("lore.font_size");
        this.loreSpan = config.getIntegerList("lore.span");

        Validate.isTrue(name || loreLines > 0, "必须为至少一个 Lore 行或显示名称启用居中设置");

        // Lore validation
        Validate.isTrue(loreLines >= 0, "Lore 行数必须为正");
        if (loreLines > 0) {
            Validate.isTrue(!loreFontSize.isEmpty(), "您必须提供至少一种 Lore 字体大小");
            Validate.isTrue(!loreSpan.isEmpty(), "您必须提供至少一个 Lore 范围");
            for (double d : loreFontSize) Validate.isTrue(d > 0, "字体大小必须为正值");
            for (double d : loreSpan) Validate.isTrue(d > 0, "字体大小必须为正值");
        }

        // Name validation
        if (name) {
            Validate.isTrue(nameFontSize > 0, "字体大小必须为正值");
            Validate.isTrue(nameSpan > 0, "范围必须是正数");
        }

        this.regex = Pattern.compile(config.getString("regex", DEFAULT_REGEX));
    }

    public boolean displayName() {
        return name;
    }

    public int getLoreLines() {
        return loreLines;
    }

    @NotNull
    public String centerLore(int j, String line) {
        return center(line, "", loreFontSize.get(Math.min(j, loreFontSize.size() - 1)), loreSpan.get(Math.min(j, loreSpan.size() - 1)));
    }

    @NotNull
    public String centerName(@NotNull String line) {
        return center(line, namePrefix, nameFontSize, nameSpan);
    }

    @NotNull
    private String center(@NotNull String line, @NotNull String prefix, double fontSize, int span) {

        // Find what to center
        final Matcher matcher = regex.matcher(line);
        if (!matcher.find()) return line;

        final int start = matcher.start(), end = matcher.end();
        // Average character size + 1 pixel per space
        final int length = (int) (fontSize * (end - start) + SEPARATOR_SPACE * countSeparators(line.substring(start, end)));

        // Cannot center as it's too big
        if (length >= span) return line;

        // Either ceil or floor, not really important
        final int offset = (span - length) / 2;
        return line.substring(0, start) + UtilityMethods.getFontSpace(offset) + prefix + line.substring(start);
    }

    private int countSeparators(@NotNull String str) {
        int count = 0;
        boolean prevSpace = true;
        for (int i = 0; i < str.length(); i++) {
            final boolean space = str.charAt(i) == ' ';
            if (!prevSpace && !space) count++;
            prevSpace = space;
        }
        return count;
    }
}
