package net.Indyuce.mmoitems.api.item.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.NameModifier.ModifierType;
import net.Indyuce.mmoitems.manager.TemplateManager;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class TemplateModifier {

	/*
	 * not really used internally apart from letting the user locate the mod in
	 * the config files
	 */
	private final String id;

	private final double chance, weight;
	private final Map<ItemStat, RandomStatData> data;
	private final NameModifier nameModifier;

	private static final Random random = new Random();

	public TemplateModifier(ConfigurationSection config) {
		this(null, config);
	}

	/**
	 * Loads an item gen modifier from a configuration section. If you provide
	 * the ItemGenManager, you will be able to use the 'parent' option to
	 * redirect that modifier to a public gen modifier.
	 * 
	 * @param manager
	 *            Provide the ItemGenManager to use the 'parent' option
	 * @param config
	 *            The configuration section to load the modifier from
	 */
	public TemplateModifier(TemplateManager manager, ConfigurationSection config) {
		Validate.notNull(config, "Could not read config");
		id = config.getName().toLowerCase().replace("_", "-");

		/*
		 * when providing a non-null itemGenManager, it indicates that public
		 * modifiers were loaded and that the constructor can use them
		 */
		if (!config.contains("stats")) {
			Validate.notNull(manager, "Cannot create a private modifier outside an item template");

			Validate.isTrue(manager.hasModifier(id), "Could not find public modifier with ID '" + id + "'");
			TemplateModifier parent = manager.getModifier(id);

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
				data.put(stat, stat.whenInitialized(config.get("stats." + key)));
			} catch (IllegalArgumentException exception) {

				if (!exception.getMessage().isEmpty()) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occurred while trying to load item gen modifier " + id + ": " + exception.getMessage()); }
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
