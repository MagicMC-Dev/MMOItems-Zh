package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.api.util.TemplateMap;

public class TemplateManager {

	/*
	 * registered mmoitem templates
	 */
	private final TemplateMap<MMOItemTemplate> templates = new TemplateMap<>();

	/*
	 * bank of item modifiers which can be used anywhere in generation templates
	 * to make item generation easier.
	 */
	private final Map<String, TemplateModifier> modifiers = new HashMap<>();

	private static final Random random = new Random();

	public boolean hasTemplate(Type type, String id) {
		return templates.hasValue(type, id);
	}

	public MMOItemTemplate getTemplate(Type type, String id) {
		return templates.getValue(type, id);
	}

	/**
	 * Used in class constructors to easily
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The item ID
	 * @return MMOItem template if it exists, or throws an IAE otherwise
	 */
	public MMOItemTemplate getTemplateOrThrow(Type type, String id) {
		Validate.isTrue(hasTemplate(type, id), "Could not find a template with ID '" + id + "'");
		return templates.getValue(type, id);
	}

	public Collection<MMOItemTemplate> getTemplates(Type type) {
		return templates.collectValues(type);
	}

	/**
	 * Registers an MMOItem template internally. Can be done at any time
	 * 
	 * @param template
	 *            Template to register
	 */
	public void registerTemplate(MMOItemTemplate template) {
		Validate.notNull(template, "MMOItem template cannot be null");

		templates.setValue(template.getType(), template.getId(), template);
	}

	/**
	 * Unregisters a template from mmoitem registery. Must be used when an item
	 * is removed from the config files.
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The item ID
	 */
	public void unregisterTemplate(Type type, String id) {
		templates.removeValue(type, id);
		MMOItems.plugin.getUpdater().disable(type, id);
	}

	/**
	 * Used whenever an item is created or edited through the GUI edition. This
	 * method unregisters the current template and loads it again from the
	 * configuration file.
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The item ID
	 */
	public void requestTemplateUpdate(Type type, String id) {
		templates.removeValue(type, id);

		try {
			registerTemplate(new MMOItemTemplate(type, type.getConfigFile().getConfig().getConfigurationSection(id)));
		} catch (IllegalArgumentException exception) {
			MMOItems.plugin.getLogger().log(Level.INFO,
					"An error occured while trying to reload item gen template '" + id + "': " + exception.getMessage());
		}
	}

	/**
	 * @deprecated Use hasTemplate(Type, String) instead
	 */
	@Deprecated
	public boolean hasMMOItem(Type type, String id) {
		return hasTemplate(type, id);
	}

	/**
	 * @return Collects all existing mmoitem templates into a set so that it can
	 *         be filtered afterwards to generate random loot
	 */
	public Collection<MMOItemTemplate> collectTemplates() {
		return templates.collectValues();
	}

	public boolean hasModifier(String id) {
		return modifiers.containsKey(id);
	}

	public TemplateModifier getModifier(String id) {
		return modifiers.get(id);
	}

	public Collection<TemplateModifier> getModifiers() {
		return modifiers.values();
	}

	public ItemTier rollTier() {

		double s = 0;
		for (ItemTier tier : MMOItems.plugin.getTiers().getAll()) {
			if (s >= 1 || random.nextDouble() < tier.getGenerationChance() / (1 - s))
				return tier;

			s += tier.getGenerationChance();
		}

		// default tier
		return null;
	}

	/**
	 * @param playerLevel
	 *            Input player level
	 * @return Generates a randomly chosen item level. The level spread
	 *         (editable in the main config file) corresponding to the standard
	 *         deviation of a gaussian distribution centered on the player level
	 *         (input)
	 */
	public int rollLevel(int playerLevel) {
		double found = random.nextGaussian() * MMOItems.plugin.getLanguage().levelSpread + playerLevel;

		// cannot be more than 2x the level and must be higher than 1
		found = Math.max(Math.min(2 * playerLevel, found), 1);

		return (int) found;
	}

	public void reload() {
		templates.clear();
		modifiers.clear();

		MMOItems.plugin.getLogger().log(Level.INFO, "Loading template modifiers, please wait..");
		for (File file : new File(MMOItems.plugin.getDataFolder() + "/generator/modifiers").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false))
				try {
					TemplateModifier modifier = new TemplateModifier(config.getConfigurationSection(key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen modifier '" + key + "': " + exception.getMessage());
				}
		}

		MMOItems.plugin.getLogger().log(Level.INFO, "Loading item templates, please wait..");
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			for (String key : config.getKeys(false))
				try {
					registerTemplate(new MMOItemTemplate(type, config.getConfigurationSection(key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen template '" + key + "': " + exception.getMessage());
				}
		}
	}
}
