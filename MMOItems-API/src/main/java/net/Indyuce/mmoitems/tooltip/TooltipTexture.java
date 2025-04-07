package net.Indyuce.mmoitems.tooltip;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.api.item.build.LoreBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TooltipTexture {
    private final String id;

    @NotNull
    private final String top, middle, bar, bottom;

    private final String alignText;
    private final List<String> loreHeader;
    private final int ignoreFirst;
    private final CenteringOptions centering;

    public final boolean debug;

    private static final String ALIGN_HIDE = UtilityMethods.getSpaceFont(-8192);

    public TooltipTexture(@NotNull ConfigurationSection config) {
        id = UtilityMethods.enumName(config.getName());

        String alignTexture = UtilityMethods.getSpaceFont(config.getInt("align_texture"));
        alignText = UtilityMethods.getSpaceFont(config.getInt("align_text"));

        top = resolveTexture(alignTexture, config.getString("top")) + alignText;
        middle = resolveTexture(alignTexture, config.getString("middle")) + alignText;
        bar = config.get("bar") != null ? resolveTexture(alignTexture, config.getString("bar")) : middle;
        bottom = resolveTexture(alignTexture, config.getString("bottom")) + ALIGN_HIDE;
        debug = config.getBoolean("debug");

        loreHeader = config.getStringList("lore_header").stream().map(this::resolveSpaces).collect(Collectors.toList());
        ignoreFirst = config.getInt("ignore_first");

        try {
            centering = config.isConfigurationSection("center_top") ? new CenteringOptions(config.getConfigurationSection("center_top")) : null;
        } catch (RuntimeException exception) {
            throw new RuntimeException("无法加载居中选项: " + exception.getMessage());
        }

        Validate.notNull(top, "工具提示顶部不能为空");
        Validate.notNull(middle, "工具提示中部不能为空");
        Validate.notNull(bottom, "工具提示底部不能为空");
    }

    private static final Pattern SPACE_PLACEHOLDER = Pattern.compile("\\{(-?\\d+)sp}");

    private String resolveSpaces(String texture) {
        return SPACE_PLACEHOLDER.matcher(texture).replaceAll(match -> UtilityMethods.getSpaceFont(Integer.parseInt(match.group(1))));
    }

    private String resolveTexture(String alignTexture, String texture) {
        texture = resolveSpaces(texture); // Space fonts
        texture = MythicLib.plugin.getPlaceholderParser().parse(null, texture); // Precompute static PAPI placeholders
        texture = MythicLib.plugin.parseColors(texture); // Precompute colors
        return alignTexture + texture;
    }

    @NotNull
    public String bakeItemName(String rawName) {

        // Center first
        if (centering != null && centering.displayName())
            rawName = centering.centerName(rawName);

        // Apply texture
        rawName = top + rawName + ALIGN_HIDE;

        return rawName;
    }

    @NotNull
    public String bakeLoreLine(int j, LoreBuilder.LineType lineType, String line, boolean skipTexture, boolean skipBar) {
        if (centering != null && j < centering.getLoreLines()) line = centering.centerLore(j, line); // Center first
        line = (skipTexture ? alignText : lineType.isBar() && !skipBar ? bar : middle) + line; // Apply texture
        if (!skipTexture) line = line + ALIGN_HIDE; // Hide vanilla tooltip
        return line;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getTop() {
        return top;
    }

    @NotNull
    public String getMiddle() {
        return middle;
    }

    @NotNull
    public String getAlignText() {
        return alignText;
    }

    @NotNull
    public String getBottom() {
        return bottom;
    }

    @NotNull
    public String getBar() {
        return bar;
    }

    public int getFirstIgnored() {
        return ignoreFirst;
    }

    @Nullable
    public CenteringOptions getCenteringOptions() {
        return centering;
    }

    @Nullable
    public List<String> getLoreHeader() {
        return loreHeader;
    }

    @NotNull
    public static String getSuffix() {
        return ALIGN_HIDE;
    }
}
