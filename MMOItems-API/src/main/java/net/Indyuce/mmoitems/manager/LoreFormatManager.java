package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LoreFormatManager implements Reloadable {
    private final Map<String, List<String>> formats = new HashMap<>();

    public void reload() {
        formats.clear();
        File dir = new File(MMOItems.plugin.getDataFolder() + "/language/lore-formats");
        for (File file : dir.listFiles())
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Validate.isTrue(config.isList("lore-format"), "Invalid lore-format! (" + file.getName() + ")");
                formats.put(file.getName().substring(0, file.getName().length() - 4), config.getStringList("lore-format"));
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load layout '" + file.getName() + "': " + exception.getMessage());
            }
    }

    public boolean hasFormat(@NotNull String id) {
        return formats.containsKey(id);
    }

    public Collection<List<String>> getFormats() {
        return formats.values();
    }

    /**
     * Find a lore format file by specifying its name
     *
     * @param prioritizedFormatNames The names of the formats to search.
     * @return The lore format first found from the ones specified, or the default one.
     */
    @NotNull
    public List<String> getFormat(@NotNull String... prioritizedFormatNames) {

        /*
         * Check each specified lore format in order, the first one
         * to succeed will be the winner.
         */
        for (String format : prioritizedFormatNames) {
            List<String> found = formats.get(format);
            if (found != null)
                return found;
        }

        // No lore format found / specified. Go with default.
        return MMOItems.plugin.getLanguage().getDefaultLoreFormat();
    }
}
