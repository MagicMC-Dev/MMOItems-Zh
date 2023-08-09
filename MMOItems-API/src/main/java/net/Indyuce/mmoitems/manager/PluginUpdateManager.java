package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.PluginUpdate;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PluginUpdateManager {

	/**
	 * The integer as key is used as reference in order to apply an update using
	 * the update command.
	 */
	private final Map<Integer, PluginUpdate> updates = new HashMap<>();


	public PluginUpdateManager() {
		register(new PluginUpdate(1,
				new String[]{"Applies a fix for skull textures values in 4.7.1.", "Texture values data storage changed in 4.7.1 due to the UUID change."},
				sender -> {

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

		register(new PluginUpdate(3,
				new String[]{"5.3.2: converts all your crafting station recipes to the newest config format.", "&cWarning, running this update will get rid of your # config file comments."},
				sender -> {

					for (File file : new File(MMOItems.plugin.getDataFolder() + "/crafting-stations").listFiles()) {
						FileConfiguration config = YamlConfiguration.loadConfiguration(file);

						if (config.contains("recipes")) {
							for (String key : config.getConfigurationSection("recipes").getKeys(false))
								try {

									List<String> ingredients = config.getStringList("recipes." + key + ".ingredients");
									List<String> newest = new ArrayList<>();

									for (String ingredient : ingredients) {
										String[] split = ingredient.split(" ");
										if (split[0].equals("mmoitem")) {
											String format = "mmoitem{type=" + split[1] + ",id=" + split[2];
											if (split.length > 3) format += ",amount=" + split[3];
											if (split.length > 4) format += ",display=\"" + split[4].replace("_", " ") + "\"";
											newest.add(format + "}");
										} else if (split[0].equals("vanilla")) {
											String format = "vanilla{type=" + split[1];
											if (split.length > 2 && !split[2].equals(".")) format += ",name=\"" + split[2] + "\"";
											if (split.length > 3) format += ",amount=" + split[3];
											if (split.length > 4) format += ",display=\"" + split[4].replace("_", " ") + "\"";
											newest.add(format + "}");
										} else {
											MMOItems.plugin.getLogger().log(Level.INFO,
													"Config Update 3: Could not match ingredient from '" + ingredient + "' from recipe '" + key + "', added it anyway.");
											newest.add(ingredient);
										}
									}

									config.set("recipes." + key + ".ingredients", newest);

									List<String> conditions = config.getStringList("recipes." + key + ".conditions");
									newest = new ArrayList<>();

									for (String condition : conditions) {
										String[] split = condition.split(" ");
										if (split[0].equalsIgnoreCase("class"))
											newest.add("class{list=\"" + condition.replace(split[0] + " ", "").replace(" ", ",") + "\"}");
										else if (split[0].equalsIgnoreCase("perms"))
											newest.add("permission{list=\"" + condition.replace(split[0] + " ", "").replace(" ", ",") + "\"}");
										else if (split[0].equalsIgnoreCase("food") || split[0].equals("mana") || split[0].equals("stamina"))
											newest.add(split[0] + "{amount=" + split[1] + "}");
										else if (split[0].equalsIgnoreCase("level")) newest.add("level{level=" + split[1] + "}");
										else if (split[0].equalsIgnoreCase("profession"))
											newest.add("profession{profession=" + split[1] + ",level=" + split[2] + "}");
										else if (split[0].equalsIgnoreCase("exp"))
											newest.add("exp{profession=" + split[1] + ",amount=" + split[2] + "}");
										else {
											MMOItems.plugin.getLogger().log(Level.INFO,
													"Config Update 3: Could not match condition from '" + condition + "' from recipe '" + key + "', added it anyway.");
											newest.add(condition);
										}
									}

									config.set("recipes." + key + ".conditions", newest);
								} catch (Exception exception) {
									MMOItems.plugin.getLogger().log(Level.INFO,
											"Config Update 3: Could not convert recipe with key '" + key + "': " + exception.getMessage());
								}

							try {
								config.save(file);
							} catch (IOException exception) {
								MMOItems.plugin.getLogger().log(Level.INFO,
										"Config Update 3: Could not save config '" + file.getName() + "': " + exception.getMessage());
							}
						}
					}
				}));

		register(new PluginUpdate(2,
				new String[]{"Enables the item updater for every item.", "&cNot recommended unless you know what you are doing.", "&e(No longer available)"},
				sender -> {
					sender.sendMessage(ChatColor.RED + "This command is no longer available.");
					sender.sendMessage(ChatColor.RED + "Please refer to the Revision System on the wiki.");
				}));

		register(new PluginUpdate(5, new String[]{"Transition to trigger types in 6.6.3", "Only scans through item configs"}, sender -> {

			for (Type type : MMOItems.plugin.getTypes().getAll()) {
				ConfigFile config = type.getConfigFile();
				for (String id : config.getConfig().getKeys(false)) {
					ConfigurationSection itemConfig = config.getConfig().getConfigurationSection(id);
					if (!itemConfig.contains("base.ability")) continue;

					for (String key : config.getConfig().getConfigurationSection(id + ".base.ability").getKeys(false)) {
						String mode = config.getConfig().getString(id + ".base.ability." + key + ".mode");
						if (mode == null) return;

						String newest = mode.equalsIgnoreCase("ON_HIT") ? "ATTACK" : mode.equalsIgnoreCase("WHEN_HIT") ? "DAMAGED" : mode;
						config.getConfig().set(id + ".base.ability." + key + ".mode", newest);
					}
				}
				config.save();
			}
		}));

		register(new PluginUpdate(4,
				new String[]{"Transforms all your current MMOItems into item templates and fixes some stat formats which have been changed.", "&cIt is REALLY important to save a backup before using this config update!"},
				sender -> {

					// fixes stat formats
					for (Type type : MMOItems.plugin.getTypes().getAll()) {
						ConfigFile config = type.getConfigFile();
						for (String id : config.getConfig().getKeys(false)) {

							// if item has base it will not convert
							if (config.getConfig().getConfigurationSection(id).contains("base")) {
								continue;
							}

							// translates items into templates
							config.getConfig().createSection(id + ".base", config.getConfig().getConfigurationSection(id).getValues(false));
							for (String statKey : config.getConfig().getConfigurationSection(id).getKeys(false))
								if (!statKey.equals("base")) config.getConfig().set(id + "." + statKey, null);

							// simple path changes
							rename(config.getConfig().getConfigurationSection(id + ".base"), "regeneration", "health-regeneration");
							rename(config.getConfig().getConfigurationSection(id + ".base"), "element.light", "element.lightness");

							// sound changes
							if (config.getConfig().getConfigurationSection(id + ".base").contains("consume-sound")) {
								rename(config.getConfig().getConfigurationSection(id + ".base"), "consume-sound", "sounds.on-consume.sound");
								config.getConfig().set(id + ".base.sounds.on-consume.volume", 1.0D);
								config.getConfig().set(id + ".base.sounds.on-consume.pitch", 1.0D);
							}

							// effects changes
							if (config.getConfig().getConfigurationSection(id + ".base").contains("effects")) {
								for (String effect : config.getConfig().getConfigurationSection(id + ".base.effects").getKeys(false)) {
									String[] split = config.getConfig().getString(id + ".base.effects." + effect).split(",");
									if (split.length > 1) {
										config.getConfig().set(id + ".base.new-effects." + effect + ".duration", Double.parseDouble(split[0]));
										config.getConfig().set(id + ".base.new-effects." + effect + ".amplifier", Double.parseDouble(split[1]));
									}
								}
								config.getConfig().set(id + ".base.effects", null);
								rename(config.getConfig().getConfigurationSection(id + ".base"), "new-effects", "effects");
							}

							if (config.getConfig().getConfigurationSection(id + ".base").contains("restore")) {
								config.getConfig().set(id + ".base.restore-health", config.getConfig().getDouble(id + ".base.restore.health"));
								config.getConfig().set(id + ".base.restore-food", config.getConfig().getDouble(id + ".base.restore.food"));
								config.getConfig()
										.set(id + ".base.restore-saturation", config.getConfig().getDouble(id + ".base.restore.saturation"));
								config.getConfig().set(id + ".base.restore", null);
							}

							// fix numeric stat formats
							for (String statKey : config.getConfig().getConfigurationSection(id + ".base").getKeys(false)) {

								String str = config.getConfig().getString(id + ".base." + statKey);
								if (str != null) try {

									String[] split = str.split("=");
									Validate.isTrue(split.length == 2);
									double val1 = Double.parseDouble(split[0]);
									double val2 = Double.parseDouble(split[1]);

									double avg = (val1 + val2) / 2;
									double max = Math.max(Math.abs(val1), Math.abs(val2));
									double rel = (max - avg) / max;

									config.getConfig().set(id + ".base." + statKey + ".base", avg);
									config.getConfig().set(id + ".base." + statKey + ".spread", rel / 3);
									config.getConfig().set(id + ".base." + statKey + ".max-spread", rel);

								} catch (Exception ignored) {
								}
							}

						}
						config.save();
					}
				}));


		register(new PluginUpdate(6, new String[]{"MMOItems 6.7 introduced individual config files for skills. This update reads your previous language folder and applies it to the new individual config files.", "This can also be used to apply an old plugin translation"}, sender -> {
			FileConfiguration abilities = new ConfigFile("/language", "abilities").getConfig();

			for (RegisteredSkill skill : MMOItems.plugin.getSkills().getAll()) {
				ConfigFile configFile = new ConfigFile("/skill", skill.getHandler().getLowerCaseId());
				FileConfiguration config = configFile.getConfig();

				// Apply old name
				config.set("name", Objects.requireNonNullElse(abilities.getString("ability." + skill.getHandler().getLowerCaseId()), skill.getName()));

				// Apply old modifier name
				for (String mod : skill.getHandler().getModifiers())
					config.set("modifier." + mod + ".name", Objects.requireNonNullElse(abilities.getString("modifier." + mod), skill.getParameterName(mod)));

				configFile.save();
			}

			sender.sendMessage("Config updates successfully applied, reloading skills..");
			MMOItems.plugin.getSkills().initialize(true);
		}));

        register(new PluginUpdate(7, new String[]{"MI 6.7 introduced a 'timer' skill modifier for all skills.",
                "This update registers that modifier in every of your skills.",
                "Has to be ran after &6/mi update apply 6&7."}, sender -> {

            for (SkillHandler<?> handler : MythicLib.plugin.getSkills().getHandlers()) {
                ConfigFile config = new ConfigFile("/skill", handler.getLowerCaseId());
                config.getConfig().set("modifier.timer.name", "Timer");
                config.getConfig().set("modifier.timer.default-value", 0d);
                config.save();
            }

            sender.sendMessage("Config updates successfully applied, reloading skills..");
            MMOItems.plugin.getSkills().initialize(true);
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

	private void rename(ConfigurationSection config, String oldPath, String newPath) {
		if (config.contains(oldPath)) {
			Object temp = config.get(oldPath);
			config.set(oldPath, null);
			config.set(newPath, temp);
		}
	}

}
