package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.LoadedCraftingObject;
import net.Indyuce.mmoitems.api.crafting.condition.*;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.IngredientType;
import net.Indyuce.mmoitems.api.crafting.ingredient.MMOItemIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.VanillaIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.MMOItemPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.VanillaPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.trigger.*;
import net.Indyuce.mmoitems.comp.mythicmobs.crafting.MythicMobsSkillTrigger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

public class CraftingManager implements Reloadable {

	/**
	 * Order matters when trying to recognize an ingredient type: if none
	 * ingredient matches, the item is considered as a vanilla item.
	 */
	private final List<IngredientType> ingredients = new ArrayList<>();
	private final Set<LoadedCraftingObject<Condition>> conditions = new HashSet<>();
	private final Set<LoadedCraftingObject<Trigger>> triggers = new HashSet<>();

	private final Map<String, CraftingStation> stations = new HashMap<>();

	public CraftingManager() {

		// Conditions
		registerCondition("level", LevelCondition::new, new ConditionalDisplay("&a" + AltChar.check + " Requires Level #level#", "&c" + AltChar.cross + " Requires Level #level#"));
		registerCondition("permission", PermissionCondition::new, new ConditionalDisplay("&a" + AltChar.check + " #display#", "&c" + AltChar.cross + " #display#"));
		registerCondition("placeholder", PlaceholderCondition::new, null);
		registerCondition("mana", ManaCondition::new, new ConditionalDisplay("&a" + AltChar.check + " Requires #mana# Mana", "&c" + AltChar.cross + " Requires #mana# Mana"));
		registerCondition("stamina", StaminaCondition::new, new ConditionalDisplay("&a" + AltChar.check + " Requires #stamina# Stamina", "&c" + AltChar.cross + " Requires #stamina# Stamina"));
		registerCondition("food", FoodCondition::new, new ConditionalDisplay("&a" + AltChar.check + " Requires #food# Food", "&c" + AltChar.cross + " Requires #food# Food"));
		registerCondition("class", ClassCondition::new, new ConditionalDisplay("&a" + AltChar.check + " Required Class: #class#", "&c" + AltChar.cross + " Required Class: #class#"));

		// Triggers
		registerTrigger("command", CommandTrigger::new);
		registerTrigger("message", MessageTrigger::new);
		registerTrigger("sound", SoundTrigger::new);
		registerTrigger("vanilla", VanillaTrigger::new);
		registerTrigger("mmoitem", MMOItemTrigger::new);

		// Ingredients
		registerIngredient("vanilla", VanillaIngredient::new, new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"), nbt -> true, VanillaPlayerIngredient::new);
		registerIngredient("mmoitem", MMOItemIngredient::new, new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #level##item#", "&c" + AltChar.cross + " &7#amount# #level##item#"), NBTItem::hasType, MMOItemPlayerIngredient::new);

		// MythicMobs native compatibility
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			/*registerIngredient("mythicitem", MythicItemIngredient::new,
					new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
					nbt -> nbt.hasTag("MYTHIC_TYPE"), MythicItemPlayerIngredient::new);*/
			registerTrigger("mmskill", MythicMobsSkillTrigger::new);
		}
	}

	public void reload() {
		stations.clear();

		ConfigFile language = new ConfigFile("/language", "crafting-stations");

		for (LoadedCraftingObject<Condition> condition : getConditions())
			if (condition.hasDisplay()) {
				String path = "condition." + condition.getId();
				if (!language.getConfig().contains(path)) {
					language.getConfig().createSection(path);
					condition.getDisplay().setup(language.getConfig().getConfigurationSection(path));
				}

				condition.setDisplay(new ConditionalDisplay(language.getConfig().getConfigurationSection(path)));
			}

		for (IngredientType ingredient : getIngredients()) {
			String path = "ingredient." + ingredient.getId();
			if (!language.getConfig().contains(path)) {
				language.getConfig().createSection(path);
				ingredient.getDisplay().setup(language.getConfig().getConfigurationSection(path));
			}

			ingredient.setDisplay(new ConditionalDisplay(language.getConfig().getConfigurationSection(path)));
		}

		language.save();

		for (File file : new File(MMOItems.plugin.getDataFolder() + "/crafting-stations").listFiles())
			try {
				CraftingStation station = new CraftingStation(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file));
				stations.put(station.getId(), station);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load station '" + file.getName() + "': " + exception.getMessage());
			}
		
		for (CraftingStation station : stations.values())
			try {
				station.postLoad();
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"Could not post-load station '" + station.getId() + "': " + exception.getMessage());
			}
	}

	public int countRecipes() {
		int t = 0;
		for (CraftingStation station : stations.values())
			t += station.getRecipes().size();
		return t;
	}

	public boolean hasStation(String id) {
		return stations.containsKey(id);
	}

	public Collection<CraftingStation> getStations() {
		return stations.values();
	}

	public CraftingStation getStation(String id) {
		return stations.get(id);
	}

	/**
	 * Finds the corresponding ingredient type, and from there
	 * load the corresponding ingredient from the line config
	 */
	@NotNull
	public Ingredient getIngredient(MMOLineConfig config) {
		String key = config.getKey();

		for (IngredientType ingredient : ingredients)
			if (ingredient.getId().equals(key))
				return ingredient.load(config);

		throw new IllegalArgumentException("Could not match ingredient");
	}

	/**
	 * Finds the corresponding condition type, and from there
	 * load the corresponding condition from the line config
	 */
	@NotNull
	public Condition getCondition(MMOLineConfig config) {
		String key = config.getKey();

		for (LoadedCraftingObject<Condition> condition : conditions)
			if (condition.getId().equalsIgnoreCase(key))
				return condition.load(config);

		throw new IllegalArgumentException("Could not match condition");
	}

	/**
	 * Finds the corresponding trigger type, and from there
	 * load the corresponding trigger from the line config
	 */
	@NotNull
	public Trigger getTrigger(MMOLineConfig config) {
		String key = config.getKey();

		for (LoadedCraftingObject<Trigger> trigger : triggers)
			if (trigger.getId().equalsIgnoreCase(key))
				return trigger.load(config);

		throw new IllegalArgumentException("Could not match trigger");
	}

	public List<IngredientType> getIngredients() {
		return ingredients;
	}

	public Set<LoadedCraftingObject<Condition>> getConditions() {
		return conditions;
	}

	public Set<LoadedCraftingObject<Trigger>> getTriggers() {
		return triggers;
	}

	/**
	 * Registers a type of ingredient in MMOItems crafting stations
	 * <p>
	 * See {@link IngredientType} for more information.
	 *
	 * @param id             The ingredient id
	 * @param function       Function that loads an ingredient from a line config
	 * @param display        How it displays in the item lore
	 * @param check          Should return true if an item can be handled by this ingredient type
	 * @param readIngredient After checking if this ingredient type can handle an item,
	 *                       method called to provide the corresponding PlayerIngredient
	 */
	public void registerIngredient(String id, Function<MMOLineConfig, Ingredient> function, ConditionalDisplay display, Predicate<NBTItem> check, Function<NBTItem, PlayerIngredient> readIngredient) {
		ingredients.add(0, new IngredientType(id, function, display, check, readIngredient));
	}

	/**
	 * Registers a type of condition in MMOItems crafting stations
	 *
	 * @param id       Condition ID
	 * @param function Function that loads a condition from a line conf
	 * @param display  How it displays in the item lore, null if it should not
	 */
	public void registerCondition(String id, Function<MMOLineConfig, Condition> function, @Nullable ConditionalDisplay display) {
		conditions.add(new LoadedCraftingObject<>(id, function, display));
	}

	/**
	 * Registers a type of trigger in MMOItems crafting stations. Unlike
	 * conditions or ingredients, triggers don't need to be displayed
	 * in the item lore therefore no 'display' argument is required.
	 *
	 * @param id       Trigger ID
	 * @param function Function that loads that type of trigger from a line configuration
	 */
	public void registerTrigger(String id, Function<MMOLineConfig, Trigger> function) {
		triggers.add(new LoadedCraftingObject<Trigger>(id, function, null));
	}

	public Collection<CraftingStation> getAll() {
		return stations.values();
	}
}
