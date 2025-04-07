package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.tooltip.TooltipTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreFormatManager implements Reloadable {
    private final Map<String, List<String>> formats = new HashMap<>();
    private final Map<String, TooltipTexture> tooltips = new HashMap<>();

    public void reload() {
        formats.clear();
        tooltips.clear();

        // Read lore formats
        FileUtils.loadObjectsFromFolder(MMOItems.plugin, "language/lore-formats", true, (key, config) -> {
            Validate.isTrue(config.isList("lore-format"), "Invalid lore-format! (" + key + ")");
            formats.put(key, config.getStringList("lore-format"));
        }, "Could not load layout '%s' from file '%s': %s");

        // Initialize tooltips folder
        if (!FileUtils.getFile(MMOItems.plugin, "tooltips").exists()) {
            FileUtils.copyDefaultFile(MMOItems.plugin, "tooltips/example_tooltips.yml");
        }

        // Load tooltips
        FileUtils.loadObjectsFromFolder(MMOItems.plugin, "tooltips", false, (name, config) -> {
            final TooltipTexture tooltip = new TooltipTexture(config);
            tooltips.put(tooltip.getId(), tooltip);
        }, "Could not load tooltip '%s' from file '%s': %s");
    }

    public boolean hasFormat(@NotNull String id) {
        return formats.containsKey(id);
    }

    @NotNull
    public Collection<List<String>> getFormats() {
        return formats.values();
    }

    public boolean hasTooltip(@NotNull String id) {
        return tooltips.containsKey(id);
    }

    @NotNull
    public Collection<TooltipTexture> getTooltips() {
        return tooltips.values();
    }

    @Nullable
    public TooltipTexture getTooltip(@NotNull String id) {
        return tooltips.get(id);
    }

    @NotNull
    public List<String> getFormat(@NotNull MMOItem mmoitem) {
        if (mmoitem.hasData(ItemStats.LORE_FORMAT)) {
            final List<String> format = formats.get(mmoitem.getData(ItemStats.LORE_FORMAT).toString());
            if (format != null) return format;
        }

        if (mmoitem.getType().getLoreFormat() != null) {
            final List<String> format = formats.get(mmoitem.getType().getLoreFormat());
            if (format != null) return format;
        }

        return MMOItems.plugin.getLanguage().getDefaultLoreFormat();
    }

    /**
     * Find a lore format file by specifying its name
     *
     * @param prioritizedFormatNames The names of the formats to search.
     * @return The lore format first found from the ones specified, or the default one.
     */
    @NotNull
    @Deprecated
    public List<String> getFormat(@NotNull String... prioritizedFormatNames) {

        /*
         * Check each specified lore format in order, the first one
         * to succeed will be the winner.
         */
        for (String format : prioritizedFormatNames) {
            List<String> found = formats.get(format);
            if (found != null) return found;
        }

        // No lore format found / specified. Go with default.
        return MMOItems.plugin.getLanguage().getDefaultLoreFormat();
    }
}
