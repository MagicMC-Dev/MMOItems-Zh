package net.Indyuce.mmoitems.api.item.build;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipTexture {

    @NotNull
    private final String top, middle, bottom;

    @Nullable
    private final String bar, suffix;

    private final List<String> loreHeader;

    public TooltipTexture(@NotNull ConfigurationSection config) {
        final String prefix = config.getString("prefix", "");
        top = prefix + config.getString("top");
        bar = config.contains("bar") && config.get("bar") != null ? prefix + config.getString("bar") : null;
        middle = prefix + config.getString("middle");
        bottom = prefix + config.getString("bottom");
        suffix = config.getString("suffix", "");
        loreHeader = config.getStringList("lore_header");

        Validate.notNull(top, "Tooltip top portion cannot be null");
        Validate.notNull(middle, "Tooltip middle portion cannot be null");
        Validate.notNull(bottom, "Tooltip bottom portion cannot be null");
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
    public String getBottom() {
        return bottom;
    }

    @NotNull
    public String getBar() {
        return bar == null ? middle : bar;
    }

    @NotNull
    public String getSuffix() {
        return suffix;
    }

    @Nullable
    public List<String> getLoreHeader() {
        return loreHeader;
    }
}
