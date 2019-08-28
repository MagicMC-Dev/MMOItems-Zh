package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.PluginUpdate;
import net.Indyuce.mmoitems.api.Type;

public class PluginUpdateManager {

	/*
	 * the integer as key is used as reference in order to apply an update using
	 * the update command.
	 */
	private Map<Integer, PluginUpdate> updates = new HashMap<>();

	public PluginUpdateManager() {
		register(new PluginUpdate(1, new String[] { "Applies a fix for skull textures values in 4.7.1.", "Texture values data storage changed in 4.7.1 due to the UUID change." }, (sender) -> {

			for (Type type : MMOItems.plugin.getTypes().getAll()) {
				ConfigFile config = type.getConfigFile();
				for (String key : config.getConfig().getKeys(false)) {
					ConfigurationSection section = config.getConfig().getConfigurationSection(key);
					if (section.contains("skull-texture") && section.get("skull-texture") instanceof String) {
						section.set("skull-texture.value", section.getString("skull-texture"));
						section.set("skull-texture.uuid", UUID.randomUUID().toString());
					}
				}

				config.save();
			}
		}));

		register(new PluginUpdate(2, new String[] { "Enables the item updater for every item.", "&cNot recommended unless you know what you are doing." }, (sender) -> {
			for (Type type : MMOItems.plugin.getTypes().getAll())
				for (String id : type.getConfigFile().getConfig().getKeys(false)) {
					String itemPath = type.getId() + "." + id;
					MMOItems.plugin.getUpdater().enable(MMOItems.plugin.getUpdater().newUpdaterData(itemPath, UUID.randomUUID(), true, true, true, true, true, true));
				}
		}));
	}

	public void register(PluginUpdate update) {
		updates.put(update.getId(), update);
	}

	public boolean has(int id) {
		return updates.containsKey(id);
	}

	public PluginUpdate get(int id) {
		return updates.get(id);
	}

	public Collection<PluginUpdate> getAll() {
		return updates.values();
	}
}
