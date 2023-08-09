package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.api.util.TemplateMap;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class TemplateManager implements Reloadable {

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

	/**
	 * @param type The MMOItem Type
	 * @param id The MMOItem ID
	 *
	 * @return If these two Strings represent a loaded MMOItem Template
	 */
	public boolean hasTemplate(@Nullable Type type, @Nullable String id) {
		if (type == null || id == null) { return false; }
		return templates.hasValue(type, id);
	}

	/**
	 * @param nbt The 'processed' ItemStack you are trying to read.
	 *
	 * @return If there is a MMOItem Template information in this NBTItem
	 */
	public boolean hasTemplate(@Nullable NBTItem nbt) {
		if (nbt == null) { return false; }
		return hasTemplate(Type.get(nbt.getType()), nbt.getString("MMOITEMS_ITEM_ID"));
	}

	/**
	 * Gets the template of such type and ID
	 *
	 * @param type Type of MMOItem
	 * @param id ID of MMOItem
	 *
	 * @return A template of these qualifications if it found them,
	 */
	@Nullable public MMOItemTemplate getTemplate(@Nullable Type type, @Nullable String id) {
		if (type == null || id == null) { return null; }
		return templates.getValue(type, id);
	}

	/**
	 * Attempts to get the Item Template of this Item Stack.
	 *
	 * @param nbt The NBTItem you are trying to read.
	 *
	 * @return The MMOItem Template parent of it, if it has any.
	 */
	@Nullable public MMOItemTemplate getTemplate(@Nullable NBTItem nbt) {
		if (nbt == null) { return null; }
		return getTemplate(Type.get(nbt.getType()), nbt.getString("MMOITEMS_ITEM_ID"));
	}

	/**
	 * Used in class constructors to easily
	 *
	 * @param  type The item type
	 * @param  id   The item ID
	 * @return      MMOItem template if it exists, or throws an IAE otherwise
	 */
	@NotNull public MMOItemTemplate getTemplateOrThrow(@Nullable Type type, @Nullable String id) {
		Validate.isTrue(type != null && hasTemplate(type, id), "Could not find a template with ID '" + id + "'");
		return templates.getValue(type, id);
	}

	@NotNull public Collection<MMOItemTemplate> getTemplates(@NotNull Type type) {
		return templates.collectValues(type);
	}
	@NotNull public ArrayList<String> getTemplateNames(@NotNull Type type) {
		ArrayList<String> names = new ArrayList<>();
		for (MMOItemTemplate t : templates.collectValues(type)) { names.add(t.getId()); }
		return names;
	}

	/**
	 * Registers an MMOItem template internally. Can be done at any time
	 *
	 * @param template Template to register
	 */
	public void registerTemplate(@NotNull MMOItemTemplate template) {
		Validate.notNull(template, "MMOItem template cannot be null");

		templates.setValue(template.getType(), template.getId(), template);
	}

	/**
	 * Unregisters a template from mmoitem registery. Must be used when an item
	 * is removed from the config files. Also disables the dynamic updater for
	 * that item
	 *
	 * @param type The item type
	 * @param id   The item ID
	 */
	public void unregisterTemplate(@NotNull Type type, @NotNull String id) {
		templates.removeValue(type, id);
	}

	/**
	 * Unregisters a template from mmoitem registery and clears it from the
	 * config file
	 *
	 * @param type The item type
	 * @param id   The item ID
	 */
	public void deleteTemplate(@NotNull Type type, @NotNull String id) {
		unregisterTemplate(type, id);

		ConfigFile config = type.getConfigFile();
		config.getConfig().set(id, null);
		config.save();
	}

	/**
	 * Used whenever an item is created or edited through the GUI edition. This
	 * method unregisters the current template and loads it again from the
	 * configuration file.
	 *
	 * Can also be used right after creating a template after the config file
	 * has been initialized in order to load the newly created item
	 *
	 * @param type The item type
	 * @param id   The item ID
	 */
	@SuppressWarnings("UnusedReturnValue")
	public MMOItemTemplate requestTemplateUpdate(@NotNull Type type, @NotNull String id) {
		templates.removeValue(type, id);

		try {
			MMOItemTemplate template = new MMOItemTemplate(type, type.getConfigFile().getConfig().getConfigurationSection(id));
			template.postLoad();
			registerTemplate(template);
			return template;

		} catch (IllegalArgumentException exception) {
			MMOItems.plugin.getLogger().log(Level.INFO,
					"An error occurred while trying to reload item gen template '" + id + "': " + exception.getMessage());
			return null;
		}
	}

	/**
	 * @return Collects all existing mmoitem templates into a set so that it can
	 *         be filtered afterwards to generate random loot
	 */
	public Collection<MMOItemTemplate> collectTemplates() { return templates.collectValues(); }

	public boolean hasModifier(String id) { return modifiers.containsKey(id); }

	public TemplateModifier getModifier(String id) { return modifiers.get(id); }

	public Collection<TemplateModifier> getModifiers() { return modifiers.values(); }

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
	 * @param  playerLevel Input player level
	 * @return             Generates a randomly chosen item level. The level
	 *                     spread (editable in the main config file)
	 *                     corresponding to the standard deviation of a gaussian
	 *                     distribution centered on the player level (input)
	 */
	public int rollLevel(int playerLevel) {
		double spread = MMOItems.plugin.getLanguage().levelSpread;
		double found = random.nextGaussian() * spread * .7 + playerLevel;

		// must be in [level - spread, level + spread]
		// lower bound must be higher than 1
		found = Math.max(Math.min(found, playerLevel + spread), Math.max(1, playerLevel - spread));

		return (int) found;
	}

	/**
	 * Templates must be loaded whenever MMOItems enables so that other plugins
	 * like MMOCore can load template references in drop items or other objects.
	 * Template data is only loaded when MMOItems enables, once sets, tiers..
	 * are initialized
	 */
	public void preloadTemplates() {
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			for (String key : config.getKeys(false))
				try {
					registerTemplate(new MMOItemTemplate(type, config.getConfigurationSection(key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO, "Could not preload item template '" + key + "': " + exception.getMessage());
				}
		}
	}

	/**
	 * Loads item generator modifiers and post load item templates.
	 */
	public void postloadTemplates() {

		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Item Templates");
		ffp.log(FriendlyFeedbackCategory.INFORMATION, "Loading template modifiers, please wait..");
		for (File file : new File(MMOItems.plugin.getDataFolder() + "/modifiers").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			ffp.activatePrefix(true, "Item Templates \u00a78($r" + file.getPath() + "\u00a78)");
			for (String key : config.getKeys(false))
				try {
					TemplateModifier modifier = new TemplateModifier(config.getConfigurationSection(key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {
					ffp.log(FriendlyFeedbackCategory.INFORMATION, "Could not load template modifier '" + key + "': " + exception.getMessage());
				}
		}

		ffp.activatePrefix(true, "Item Templates");
		ffp.log(FriendlyFeedbackCategory.INFORMATION, "Loading item templates, please wait...");
		templates.forEach(template -> {
			try {
				template.postLoad();
			} catch (IllegalArgumentException exception) {
				ffp.activatePrefix(true, "Item Templates \u00a78($r" + template.getType().getId() + "\u00a78)");
				ffp.log(FriendlyFeedbackCategory.INFORMATION, "Could not load item template '" + template.getId() + "': " + exception.getMessage());
			}
		});

		// Print all failures
		ffp.sendTo(FriendlyFeedbackCategory.INFORMATION, MMOItems.getConsole());
	}

	/**
	 * Reloads the item templates. This is the method used to reload the manager
	 * when the server is already running. It clears all the maps and loads
	 * everything again. Template references in other plugins like MMOCore must
	 * be refreshed afterwards.
	 */
	public void reload() {
		templates.clear();
		modifiers.clear();

		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Item Templates");
		ffp.log(FriendlyFeedbackCategory.INFORMATION, "Loading template modifiers, please wait..");

		for (File file : new File(MMOItems.plugin.getDataFolder() + "/modifiers").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			ffp.activatePrefix(true, "Item Templates \u00a78($r" + file.getPath() + "\u00a78)");
			for (String key : config.getKeys(false))
				try {
					TemplateModifier modifier = new TemplateModifier(config.getConfigurationSection(key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {

					// Log
					ffp.log(FriendlyFeedbackCategory.INFORMATION, "Could not load template modifier '" + key + "': " + exception.getMessage());
				}
		}

		ffp.activatePrefix(true, "Item Templates");
		ffp.log(FriendlyFeedbackCategory.INFORMATION, "Loading item templates, please wait...");
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			ffp.activatePrefix(true, "Item Templates \u00a78($r" + type.getId() + "\u00a78)");
			for (String key : config.getKeys(false))
				try {
					MMOItemTemplate template = new MMOItemTemplate(type, config.getConfigurationSection(key));
					template.postLoad();
					registerTemplate(template);

				} catch (IllegalArgumentException exception) {

					// Log
					ffp.log(FriendlyFeedbackCategory.INFORMATION, "Could not load item template '" + key + "': " + exception.getMessage());
				}
		}

		// Print all failures
		ffp.sendTo(FriendlyFeedbackCategory.INFORMATION, MMOItems.getConsole());
	}
}
