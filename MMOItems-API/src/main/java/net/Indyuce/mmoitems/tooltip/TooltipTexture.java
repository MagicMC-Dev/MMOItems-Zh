package net.Indyuce.mmoitems.tooltip;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipTexture {
    private final String id;

    @NotNull
    private final String top, middle, bottom;

    @Nullable
    private final String bar, hideVanilla, alignText;

    private final List<String> loreHeader;
    private final int ignoreFirst;
    private final CenteringOptions centering;

    public TooltipTexture(@NotNull ConfigurationSection config) {
        id = UtilityMethods.enumName(config.getName());

        final String alignLeft = UtilityMethods.getFontSpace(config.getInt("align_texture"));
        alignText = UtilityMethods.getFontSpace(config.getInt("align_text"));
        hideVanilla = UtilityMethods.getFontSpace(config.getInt("hide_texture"));

        top = alignLeft + config.getString("top") + alignText;
        loreHeader = config.getStringList("lore_header");
        middle = alignLeft + config.getString("middle") + alignText;
        bar = config.get("bar") != null ? alignLeft + config.getString("bar") : null;
        bottom = alignLeft + config.getString("bottom");

        ignoreFirst = config.getInt("ignore_first");

        try {
            centering = config.isConfigurationSection("center") ? new CenteringOptions(config.getConfigurationSection("center")) : null;
        } catch (RuntimeException exception) {
            throw new RuntimeException("Could not load centering options: " + exception.getMessage());
        }

        Validate.notNull(top, "Tooltip top portion cannot be null");
        Validate.notNull(middle, "Tooltip middle portion cannot be null");
        Validate.notNull(bottom, "Tooltip bottom portion cannot be null");
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
        return bar == null ? middle : bar;
    }

    @NotNull
    public String getSuffix() {
        return hideVanilla;
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
}
