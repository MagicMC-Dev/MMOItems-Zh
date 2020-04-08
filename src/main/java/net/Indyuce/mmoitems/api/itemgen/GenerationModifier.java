package net.Indyuce.mmoitems.api.itemgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.itemgen.NameModifier.ModifierType;
import net.Indyuce.mmoitems.manager.ItemGenManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GenerationModifier {

	/*
	 * not really used internally apart from letting the user locate the mod in
	 * the config files
	 */
	private final String id;

	private final double chance, weight;
	private final Map<ItemStat, RandomStatData> data;
	private final NameModifier nameModifier;

	private static final Random random = new Random();

	public GenerationModifier(ConfigurationSection config) {
		this(null, config);
	}

	public GenerationModifier(ItemGenManager manager, ConfigurationSection config) {
		Validate.notNull(config, "Could not read config");
		id = config.getName().toLowerCase().replace("_", "-");

		/*
		 * when providing a non-null itemGenManager, it indicates that public
		 * modifiers were loaded and that the constructor can them
		 */
		if (manager != null && config.contains("parent")) {
			String parentFormat = config.get("parent").toString().toLowerCase().replace("_", "-").replace(" ", "_");
			Validate.isTrue(manager.hasModifier(parentFormat), "Could not find public modifier with ID '" + parentFormat + "'");
			GenerationModifier parent = manager.getModifier(parentFormat);

			chance = Math.max(Math.min(config.getDouble("chance", parent.chance), 1), 0);
			weight = config.getDouble("weight", parent.weight);
			nameModifier = parent.nameModifier;
			data = parent.data;
			return;
		}

		this.data = new HashMap<>();
		this.chance = Math.max(Math.min(config.getDouble("chance", 1), 1), 0);
		this.weight = config.getDouble("weight");

		Validate.isTrue(chance > 0, "Chance must be greater than 0 otherwise useless");
		this.nameModifier = config.contains("suffix") ? new NameModifier(ModifierType.SUFFIX, config.get("suffix"))
				: config.contains("prefix") ? new NameModifier(ModifierType.PREFIX, config.get("prefix")) : null;

		Validate.notNull(config.getConfigurationSection("stats"), "Could not find base item data");
		for (String key : config.getConfigurationSection("stats").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				data.put(stat, stat.whenInitializedGeneration(config.get("stats." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO,
						"An error occured while trying to load item gen modifier " + id + ": " + exception.getMessage());
			}
	}

	public String getId() {
		return id;
	}

	public double getWeight() {
		return weight;
	}

	public Map<ItemStat, RandomStatData> getItemData() {
		return data;
	}

	public NameModifier getNameModifier() {
		return nameModifier;
	}

	public boolean hasNameModifier() {
		return nameModifier != null;
	}

	public boolean rollChance() {
		return random.nextDouble() < chance;
	}
}