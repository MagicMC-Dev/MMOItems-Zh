package net.Indyuce.mmoitems.api.itemgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.stat.type.ItemGenerationStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GenerationTemplate {
	private final String id;
	private final Type type;

	private final GaussianLinearValue weight;

	// base item data
	private final Map<ItemStat, RandomStatData> base = new HashMap<>();

	private final Set<GenerationModifier> modifiers = new HashSet<>();

	public GenerationTemplate(ConfigurationSection config) {
		Validate.notNull(config, "Could not load item gen template config");

		this.id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

		Validate.isTrue(config.contains("type"), "Could not find item gen type");
		String typeFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(typeFormat));
		type = MMOItems.plugin.getTypes().get(typeFormat);

		Validate.notNull(config.getConfigurationSection("weight"), "Could not find item gen weight");
		weight = new GaussianLinearValue(config.getConfigurationSection("weight"));

		for (String key : config.getConfigurationSection("modifiers").getKeys(false))
			modifiers.add(new GenerationModifier(config.getConfigurationSection("modifiers." + key)));

		Validate.notNull(config.getConfigurationSection("base"), "Could not find base item data");
		for (String key : config.getConfigurationSection("base").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				Validate.isTrue(stat instanceof ItemGenerationStat, "Stat " + stat.getId() + " does not support item gen!");

				base.put(stat, ((ItemGenerationStat) stat).whenInitializedGeneration(config.get("base." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO,
						"An error occured loading base item data '" + key + "' from item gen template '" + id + "': " + exception.getMessage());
			}
	}

	public Map<ItemStat, RandomStatData> getBaseItemData() {
		return base;
	}

	public Set<GenerationModifier> getModifiers() {
		return modifiers;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	/*
	 * TODO can be used by lootchests and commands to only generate items which
	 * match the player class
	 */
	public boolean acceptsClass(String profess) {
		return true;
		// return !base.containsKey(ItemStat.REQUIRED_CLASS) ||
		// ((ListStringData) base.get(ItemStat.REQUIRED_CLASS));
	}

	public double calculateWeight(int level) {
		return weight.calculate(level);
	}

	public GeneratedItemBuilder newBuilder(int playerLevel, double sd) {
		return new GeneratedItemBuilder(this, playerLevel, sd);
	}
}
