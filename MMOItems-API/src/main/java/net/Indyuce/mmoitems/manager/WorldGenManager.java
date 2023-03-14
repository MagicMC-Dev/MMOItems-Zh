package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.block.WorldGenTemplate;
import net.Indyuce.mmoitems.api.world.MMOBlockPopulator;
import net.Indyuce.mmoitems.listener.WorldGenerationListener;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WorldGenManager implements Reloadable {
    private final Map<String, WorldGenTemplate> templates = new HashMap<>();

    /*
     * maps a custom block to the world generator template so that it is later
     * easier to access all the blocks which must be placed when generating a
     * world.
     */
    private final Map<CustomBlock, WorldGenTemplate> assigned = new HashMap<>();

    private WorldGenerationListener listener;

    public WorldGenManager() {
        /*
         * load the worldGenManager even if world gen is not enabled so that if
         * admins temporarily disable it, there is no console error spam saying
         * MI could not find corresponding gen template in config
         */
        reload();
    }

    public WorldGenTemplate getOrThrow(String id) {
        Validate.isTrue(templates.containsKey(id), "Could not find gen template with ID '" + id + "'");

        return templates.get(id);
    }

    /*
     * it is mandatory to call this function after registering the custom block
     * if you want the custom block to be spawning in the worlds
     */
    public void assign(CustomBlock block, WorldGenTemplate template) {
        Validate.notNull(template, "Cannot assign a null template to a custom block");

        assigned.put(block, template);
    }

    public void reload() {
        // Listener
        if (listener != null)
            HandlerList.unregisterAll(listener);

        assigned.clear();
        templates.clear();

        FileConfiguration config = new ConfigFile("gen-templates").getConfig();
        for (String key : config.getKeys(false)) {
            try {
                WorldGenTemplate template = new WorldGenTemplate(config.getConfigurationSection(key));
                templates.put(template.getId(), template);
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "An error occurred when loading gen template '" + key + "': " + exception.getMessage());
            }
        }

        // Listeners
        if (MMOItems.plugin.getLanguage().worldGenEnabled) {
            listener = new WorldGenerationListener(this);
            Bukkit.getPluginManager().registerEvents(listener, MMOItems.plugin);
        }
    }

    public void unload() {
        if (listener != null)
            HandlerList.unregisterAll(listener);
    }

    public MMOBlockPopulator populator(@NotNull World world) {
        return new MMOBlockPopulator(world, this);
    }

    public Map<CustomBlock, WorldGenTemplate> assigned() {
        return assigned;
    }
}
