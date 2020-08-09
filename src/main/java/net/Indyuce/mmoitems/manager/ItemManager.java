package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class ItemManager {

	/*
	 * registered mmoitem templates
	 */
	private final Map<Type, Map<String, MMOItemTemplate>> templates = new HashMap<>();

	/*
	 * bank of item modifiers which can be used anywhere in generation templates
	 * to make item generation easier.
	 */
	private final Map<String, TemplateModifier> modifiers = new HashMap<>();

	private static final Random random = new Random();

	public MMOItemTemplate getTemplate(Type type, String id) {

		Validate.isTrue(templates.containsKey(type), "No template is registered with type " + type.getId() + "");
		Map<String, MMOItemTemplate> templates = this.templates.get(type);

		id = id.toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(templates.containsKey(id), "No template was found with ID '" + id + "'");

		return templates.get(id);
	}

	public MMOItem generateMMOItem(Type type, String id, PlayerData player) {
		return getTemplate(type, id).newBuilder(player.getRPG()).build();
	}

	@Deprecated
	public MMOItem getMMOItem(Type type, String id) {
		return getTemplate(type, id).newBuilder(0, rollTier()).build();
	}

	@Deprecated
	public ItemStack getItem(Type type, String id) {
		return getMMOItem(type, id).newBuilder().build();
	}

	/**
	 * Registers an MMOItem template internally. Can be done at any time
	 * 
	 * @param template
	 *            Template to register
	 */
	public void registerTemplate(MMOItemTemplate template) {
		Validate.notNull(template, "MMOItem template cannot be null");

		if (!templates.containsKey(template.getType()))
			templates.put(template.getType(), new HashMap<>());
		templates.get(template.getType()).put(template.getId(), template);
	}

	@Deprecated
	public boolean hasMMOItem(Type type, String id) {
		return hasTemplate(type, id);
	}

	public boolean hasTemplate(Type type, String id) {
		id = id.toUpperCase().replace("-", "_").replace(" ", "_");
		return templates.containsKey(type) && templates.get(type).containsKey(id);
	}

	public Collection<TemplateModifier> getModifiers() {
		return modifiers.values();
	}

	public boolean hasModifier(String id) {
		return modifiers.containsKey(id);
	}

	public TemplateModifier getModifier(String id) {
		return modifiers.get(id);
	}

	public ItemTier rollTier() {

		double s = 0;
		for (ItemTier tier : MMOItems.plugin.getTiers().getAll()) {
			if (random.nextDouble() < tier.getGenerationChance() / (1 - s))
				return tier;

			s += tier.getGenerationChance();
		}

		// default tier
		return null;
	}

	/*
	 * formula to generate the item level. input is the player level and the
	 * level spread which corresponds to the standard deviation of a gaussian
	 * distribution centered on the player level
	 */
	public int rollLevel(int playerLevel) {
		double found = random.nextGaussian() * MMOItems.plugin.getLanguage().levelSpread + playerLevel;

		// cannot be more than 2x the level and must be higher than 1
		found = Math.max(Math.min(2 * playerLevel, found), 1);

		return (int) found;
	}

	/**
	 * @return Collects all existing mmoitem templates into a set so that it can
	 *         be filtered afterwards to generate random loot
	 */
	public Set<MMOItemTemplate> collectTemplates() {
		Set<MMOItemTemplate> templates = new HashSet<>();
		this.templates.values().forEach(map -> templates.addAll(map.values()));
		return templates;
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
					registerTemplate(new MMOItemTemplate(config.getConfigurationSection(key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured while trying to load item gen template '" + key + "': " + exception.getMessage());
				}
		}
	}
}
