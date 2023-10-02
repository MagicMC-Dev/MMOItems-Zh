package net.Indyuce.mmoitems.util;

import net.Indyuce.mmoitems.api.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipTexture {

    @NotNull
    private final String top, middle, bottom;

    @Nullable
    private final String bar;

    @Deprecated
    public static TooltipTexture TEST;

    static {
        FileConfiguration configuration = new ConfigFile("tooltips").getConfig();
        TEST = new TooltipTexture(configuration.getConfigurationSection("test"));
    }

    public TooltipTexture(@NotNull ConfigurationSection config) {
        top = config.getString("top");
        bar = notEmptyOrNull(config.getString("bar"));
        middle = config.getString("middle");
        bottom = config.getString("bottom");

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

    @Nullable
    private String notEmptyOrNull(@Nullable String str) {
        return str == null || str.isEmpty() ? null : str;
    }
}
