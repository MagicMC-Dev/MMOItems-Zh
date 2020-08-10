package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigFile {
	private final Plugin plugin;
	private final String path, name;

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

	public ConfigFile(Plugin plugin, String path, String name) {
		this.plugin = plugin;
		this.path = path;
		this.name = name;

		config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + path, name + ".yml"));
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void save() {
		try {
			config.save(new File(plugin.getDataFolder() + path, name + ".yml"));
		} catch (IOException e2) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml");
		}
	}

	public void setup() {
		try {
			if (!new File(plugin.getDataFolder() + path).exists())
				new File(plugin.getDataFolder() + path).mkdir();

			if (!new File(plugin.getDataFolder() + path, name + ".yml").exists()) {
				new File(plugin.getDataFolder() + path, name + ".yml").createNewFile();
			}
		} catch (IOException e) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not generate " + name + ".yml");
		}
	}

	@Deprecated
	public void registerTemplateEdition(Type type, String id) {

		/*
		 * uncaches the item so it can be generated to apply newest changes in
		 * case the same inventory is opened again.
		 */
		// MMOItems.plugin.getItems().uncache(type, id);

		/*
		 * update the database UUID for the dynamic item updater
		 */
		if (MMOItems.plugin.getUpdater().hasData(type, id))
			MMOItems.plugin.getUpdater().getData(type, id).setUniqueId(UUID.randomUUID());

		/*
		 * finally saves the changes
		 */
		save();
	}
}