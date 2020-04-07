package net.Indyuce.mmoitems.api.itemgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemGenerationStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GenerationModifier {

	/*
	 * not really used internally apart from letting the user locate the mod in
	 * the config files
	 */
	private final String id;

	private final double chance;
	private final Map<ItemStat, RandomStatData> data = new HashMap<>();
	private final NameModifier nameMod;

	private static final Random random = new Random();

	public GenerationModifier(ConfigurationSection config) {
		Validate.notNull(config, "Could not read config");
		id = config.getName().toLowerCase().replace("_", "-");

		this.chance = config.getDouble("chance");
		
		Validate.isTrue(chance > 0, "Chance must be greater than 0 otherwise useless");
		this.nameMod = config.contains("suffix") ? new NameModifier(config.getConfigurationSection("suffix"))
				: config.contains("prefix") ? new NameModifier(config.getConfigurationSection("prefix")) : null;

		Validate.notNull(config.getConfigurationSection("stats"), "Could not find base item data");
		for (String key : config.getConfigurationSection("stats").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				Validate.isTrue(stat instanceof ItemGenerationStat, "Stat " + stat.getId() + " does not support item gem!");

				data.put(stat, ((ItemGenerationStat) stat).whenInitializedGeneration(config.get("stats." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO, "An error occured loading item gen modifier " + id + ": " + exception.getMessage());
			}
	}

	public Map<ItemStat, RandomStatData> getItemData() {
		return data;
	}

	public NameModifier getNameModifier() {
		return nameMod;
	}

	public boolean hasNameModifier() {
		return nameMod != null;
	}

	// TODO implement weight system.
	public boolean rollChance() {
		return random.nextDouble() < chance;
	}
}
