package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.itemgen.GenerationModifier;
import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;
import net.Indyuce.mmoitems.api.itemgen.tier.RandomTierInfo;
import net.Indyuce.mmoitems.api.itemgen.tier.RolledTier;

public class ItemGenManager {
	private final Map<String, GenerationTemplate> templates = new HashMap<>();

	/*
	 * bank of item modifiers which can be used anywhere in generation templates
	 * to make item generation easier.
	 */
	private final Map<String, GenerationModifier> modifiers = new HashMap<>();

	/*
	 * tiers which the item generator can use to determine how much modifier
	 * capacity an item has. plugin has a default capacity calculator in case
	 * none is specified by the user but it's best to configure it
	 */
	private final Map<ItemTier, RandomTierInfo> itemGenTiers = new LinkedHashMap<>();
	private RandomTierInfo defaultTier;

	/*
	 * config options that must be updated and that are cached here for easier
	 * calculations
	 */
	private double levelSpread;

	private static final Random random = new Random();

	public ItemGenManager() {
		reload();
	}

	public Collection<GenerationTemplate> getTemplates() {
		return templates.values();
	}

	public Collection<GenerationModifier> getModifiers() {
		return modifiers.values();
	}

	public boolean hasTemplate(String id) {
		return templates.containsKey(id);
	}

	public boolean hasModifier(String id) {
		return modifiers.containsKey(id);
	}

	public GenerationTemplate getTemplate(String id) {
		return templates.get(id);
	}

	public GenerationModifier getModifier(String id) {
		return modifiers.get(id);
	}

	public RandomTierInfo getTierInfo(ItemTier tier) {
		return itemGenTiers.getOrDefault(tier, defaultTier);
	}

	public void reload() {
		templates.clear();
		itemGenTiers.clear();
		modifiers.clear();

		for (File file : new File(MMOItems.plugin.getDataFolder() + "/generator/modifiers").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false))
				try {
					GenerationModifier modifier = new GenerationModifier(config.getConfigurationSection(key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen modifier '" + key + "': " + exception.getMessage());
				}
		}

		for (File file : new File(MMOItems.plugin.getDataFolder() + "/generator/templates").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false))
				try {
					GenerationTemplate template = new GenerationTemplate(config.getConfigurationSection(key));
					templates.put(template.getId(), template);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen template '" + key + "': " + exception.getMessage());
				}
		}

		FileConfiguration config = new ConfigFile("/generator", "config").getConfig();

		levelSpread = config.getDouble("item-level-spread");

		for (String key : config.getConfigurationSection("tiers").getKeys(false))
			if (!key.equalsIgnoreCase("default"))
				try {
					RandomTierInfo info = new RandomTierInfo(config.getConfigurationSection("tiers." + key));
					itemGenTiers.put(info.getTier(), info);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen tier capacity formula which ID '" + key + "': " + exception.getMessage());
				}

		try {
			defaultTier = new RandomTierInfo(new NumericStatFormula(config.getConfigurationSection("tiers.default.capacity")));
		} catch (IllegalArgumentException exception) {
			defaultTier = new RandomTierInfo(new NumericStatFormula(5, .05, .1, .3));
			MMOItems.plugin.getLogger().log(Level.INFO,
					"An error occured while trying to load default capacity formula for the item generator, using default: "
							+ exception.getMessage());
		}
	}

	public RolledTier rollTier(int itemLevel) {

		double s = 0;
		for (RandomTierInfo tier : itemGenTiers.values()) {
			if (random.nextDouble() < tier.getChance() / (1 - s))
				return tier.roll(itemLevel);

			s += tier.getChance();
		}

		// default tier
		return defaultTier.roll(itemLevel);
	}

	/*
	 * formula to generate the item level. input is the player level and the
	 * level spread which corresponds to the standard deviation of a gaussian
	 * distribution centered on the player level
	 */
	public int rollLevel(int playerLevel) {
		double found = random.nextGaussian() * levelSpread + playerLevel;

		// cannot be more than 2x the level and must be higher than 1
		found = Math.max(Math.min(2 * playerLevel, found), 1);

		return (int) found;
	}
}
