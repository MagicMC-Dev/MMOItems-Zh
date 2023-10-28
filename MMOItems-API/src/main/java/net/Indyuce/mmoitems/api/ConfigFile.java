package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.ItemReference;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigFile {
    private final Plugin plugin;
    private final String path, name;
    private final boolean exists;
    private final FileConfiguration config;

    public ConfigFile(String name) {
        this(MMOItems.plugin, "", name);
    }

    public ConfigFile(Plugin plugin, String name) {
        this(plugin, "", name);
    }

    public ConfigFile(String path, String name) {
        this(MMOItems.plugin, path, name);
    }

    /**
     * Configuration file path is /server_root/plugins/plugin_name/path/name
     *
     * @param plugin Plugin owning config file
     * @param path   Folder path
     * @param name   Config file name
     */
    public ConfigFile(Plugin plugin, String path, String name) {
        this.plugin = plugin;
        this.path = path;
        this.name = name;

        final File file = new File(plugin.getDataFolder() + path, name + ".yml");
        exists = file.exists();
        config = YamlConfiguration.loadConfiguration(file);
    }

    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    public boolean exists() {
        return exists;
    }

    public void save() {
        try {
            config.save(new File(plugin.getDataFolder() + path, name + ".yml"));
        } catch (IOException exception) {
            MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml: " + exception.getMessage());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setup() {
        try {
            if (!new File(plugin.getDataFolder() + path).exists())
                new File(plugin.getDataFolder() + path).mkdir();

            if (!new File(plugin.getDataFolder() + path, name + ".yml").exists()) {
                new File(plugin.getDataFolder() + path, name + ".yml").createNewFile();
            }
        } catch (IOException exception) {
            MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not generate " + name + ".yml: " + exception.getMessage());
        }
    }

    public void registerTemplateEdition(ItemReference ref) {

        // Saves the changes before asking for a template update
        save();

        /*
         * Goes for a template update once the change has been saved. this
         * simply unloads the currently saved template and reloads it
         */
        MMOItems.plugin.getTemplates().requestTemplateUpdate(ref.getType(), ref.getId());

		/* Update the database UUID for the dynamic item updater
		if (MMOItems.plugin.getUpdater().hasData(ref))
			MMOItems.plugin.getUpdater().getData(ref).setUniqueId(UUID.randomUUID());*/
    }
}