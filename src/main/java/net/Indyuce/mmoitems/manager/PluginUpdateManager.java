package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
		register(new PluginUpdate(1, new String[] { "Applies a fix for skull textures values in 4.7.1.", "Texture values data storage changed in 4.7.1 due to the UUID change." }, sender -> {

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

		register(new PluginUpdate(3, new String[] { "5.3.2: converts all your crafting station recipes to the newest config format.", "&cWarning, running this update will get rid of your # config file comments." }, sender -> {

			for (File file : new File(MMOItems.plugin.getDataFolder() + "/crafting-stations").listFiles()) {
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);

				if (config.contains("recipes")) {
					for (String key : config.getConfigurationSection("recipes").getKeys(false))
						try {

							List<String> ingredients = config.getStringList("recipes." + key + ".ingredients");
							List<String> newest = new ArrayList<String>();

							for (String ingredient : ingredients) {
								String[] split = ingredient.split("\\ ");
								if (split[0].equals("mmoitem")) {
									String format = "mmoitem{type=" + split[1] + ",id=" + split[2];
									if (split.length > 3)
										format += ",amount=" + split[3];
									if (split.length > 4)
										format += ",display=\"" + split[4].replace("_", " ") + "\"";
									newest.add(format + "}");
								}

								else if (split[0].equals("vanilla")) {
									String format = "vanilla{type=" + split[1];
									if (split.length > 2 && !split[2].equals("."))
										format += ",name=\"" + split[2] + "\"";
									if (split.length > 3)
										format += ",amount=" + split[3];
									if (split.length > 4)
										format += ",display=\"" + split[4].replace("_", " ") + "\"";
									newest.add(format + "}");
								}

								else {
									MMOItems.plugin.getLogger().log(Level.INFO, "Config Update 3: Could not match ingredient from '" + ingredient + "' from recipe '" + key + "', added it anyway.");
									newest.add(ingredient);
								}
							}

							config.set("recipes." + key + ".ingredients", newest);

							List<String> conditions = config.getStringList("recipes." + key + ".conditions");
							newest = new ArrayList<>();

							for (String condition : conditions) {
								String[] split = condition.split("\\ ");
								if (split[0].equalsIgnoreCase("class"))
									newest.add("class{list=\"" + condition.replace(split[0] + " ", "").replace(" ", ",") + "\"}");
								else if (split[0].equalsIgnoreCase("perms"))
									newest.add("permission{list=\"" + condition.replace(split[0] + " ", "").replace(" ", ",") + "\"}");
								else if (split[0].equalsIgnoreCase("food") || split[0].equals("mana") || split[0].equals("stamina"))
									newest.add(split[0] + "{amount=" + split[1] + "}");
								else if (split[0].equalsIgnoreCase("level"))
									newest.add("level{level=" + split[1] + "}");
								else if (split[0].equalsIgnoreCase("profession"))
									newest.add("profession{profession=" + split[1] + ",level=" + split[2] + "}");
								else if (split[0].equalsIgnoreCase("exp"))
									newest.add("exp{profession=" + split[1] + ",amount=" + split[2] + "}");
								else {
									MMOItems.plugin.getLogger().log(Level.INFO, "Config Update 3: Could not match condition from '" + condition + "' from recipe '" + key + "', added it anyway.");
									newest.add(condition);
								}
							}

							config.set("recipes." + key + ".conditions", newest);
						} catch (Exception exception) {
							MMOItems.plugin.getLogger().log(Level.INFO, "Config Update 3: Could not convert recipe with key '" + key + "': " + exception.getMessage());
						}

					try {
						config.save(file);
					} catch (IOException exception) {
						MMOItems.plugin.getLogger().log(Level.INFO, "Config Update 3: Could not save config '" + file.getName() + "': " + exception.getMessage());
					}
				}
			}
		}));

		register(new PluginUpdate(2, new String[] { "Enables the item updater for every item.", "&cNot recommended unless you know what you are doing." }, sender -> {
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
