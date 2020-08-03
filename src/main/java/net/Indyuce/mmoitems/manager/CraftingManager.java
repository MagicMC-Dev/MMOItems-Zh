package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

import net.Indyuce.mmoitems.api.crafting.trigger.*;
import net.Indyuce.mmoitems.comp.mythicmobs.crafting.MythicItemIngredient;
import net.Indyuce.mmoitems.comp.mythicmobs.crafting.MythicMobsSkillTrigger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.condition.ClassCondition;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.condition.FoodCondition;
import net.Indyuce.mmoitems.api.crafting.condition.IngredientCondition;
import net.Indyuce.mmoitems.api.crafting.condition.LevelCondition;
import net.Indyuce.mmoitems.api.crafting.condition.ManaCondition;
import net.Indyuce.mmoitems.api.crafting.condition.PermissionCondition;
import net.Indyuce.mmoitems.api.crafting.condition.StaminaCondition;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.MMOItemIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.VanillaIngredient;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.api.item.NBTItem;

public class CraftingManager {

	/*
	 * order matters when trying to recognize an ingredient type: if none
	 * ingredient matches, the item is considered as a vanilla item.
	 */
	private final List<IngredientType> ingredients = new ArrayList<>();
	private final Set<LoadedObject<Condition>> conditions = new HashSet<>();
	private final Set<LoadedObject<Trigger>> triggers = new HashSet<>();

	private final Map<String, CraftingStation> stations = new HashMap<>();

	public CraftingManager() {
		// conditions
		registerCondition("level", config -> new LevelCondition(config), new ConditionalDisplay("&a" + AltChar.check + " Requires Level #level#", "&c" + AltChar.cross + " Requires Level #level#"));
		registerCondition("permission", config -> new PermissionCondition(config), null);
		registerCondition("mana", config -> new ManaCondition(config), new ConditionalDisplay("&a" + AltChar.check + " Requires #mana# Mana", "&c" + AltChar.cross + " Requires #mana# Mana"));
		registerCondition("stamina", config -> new StaminaCondition(config), new ConditionalDisplay("&a" + AltChar.check + " Requires #stamina# Stamina", "&c" + AltChar.cross + " Requires #stamina# Stamina"));
		registerCondition("food", config -> new FoodCondition(config), new ConditionalDisplay("&a" + AltChar.check + " Requires #food# Food", "&c" + AltChar.cross + " Requires #food# Food"));
		registerCondition("class", config -> new ClassCondition(config), new ConditionalDisplay("&a" + AltChar.check + " Required Class: #class#", "&c" + AltChar.cross + " Required Class: #class#"));
		registerCondition("ingredient", config -> new IngredientCondition(), null);

		// triggers
		registerTrigger("command", config -> new CommandTrigger(config));
		registerTrigger("message", config -> new MessageTrigger(config));
		registerTrigger("sound", config -> new SoundTrigger(config));
		registerTrigger("vanilla", config -> new VanillaTrigger(config));
		registerTrigger("mmoitem", config -> new MMOItemTrigger(config));

		// ingredients
		registerIngredient("vanilla", config -> new VanillaIngredient(config), new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"), nbt -> true, item -> item.getItem().getType().name().toLowerCase() + "_" + (item.getItem().hasItemMeta() ? item.getItem().getItemMeta().getDisplayName() : null));
		registerIngredient("mmoitem", config -> new MMOItemIngredient(config), new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #level##item#", "&c" + AltChar.cross + " &7#amount# #level##item#"), nbt -> nbt.hasType(), item -> {
			String upgradeString = item.getString("MMOITEMS_UPGRADE");
			int level = !upgradeString.isEmpty() ? new JsonParser().parse(upgradeString).getAsJsonObject().get("Level").getAsInt() : 0;
			return item.getString("MMOITEMS_ITEM_TYPE").toLowerCase() + (level != 0 ? "-" + level : "") + "_" + item.getString("MMOITEMS_ITEM_ID").toLowerCase();
		});

		// mm comp
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			registerIngredient("mythicitem", config -> new MythicItemIngredient(config),
					new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
					nbt -> nbt.hasTag("MYTHIC_TYPE"), nbt -> nbt.getString("MYTHIC_TYPE").toLowerCase());
			registerTrigger("mmskill", config -> new MythicMobsSkillTrigger(config));
		}
	}

	public void reload() {
		stations.clear();

		ConfigFile language = new ConfigFile("/language", "crafting-stations");

		for (LoadedObject<Condition> condition : getConditions())
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

	public Ingredient getIngredient(MMOLineConfig config) {
		String key = config.getKey();

		for (IngredientType ingredient : ingredients)
			if (ingredient.getId().equals(key))
				return ingredient.load(config);

		return null;
	}

	public Condition getCondition(MMOLineConfig config) {
		String key = config.getKey();

		for (LoadedObject<Condition> condition : conditions)
			if (condition.getId().equalsIgnoreCase(key))
				return condition.load(config);

		return null;
	}

	public Trigger getTrigger(MMOLineConfig config) {
		String key = config.getKey();

		for (LoadedObject<Trigger> trigger : triggers)
			if (trigger.getId().equalsIgnoreCase(key))
				return trigger.load(config);

		return null;
	}

	public List<IngredientType> getIngredients() {
		return ingredients;
	}

	public Set<LoadedObject<Condition>> getConditions() {
		return conditions;
	}

	public Set<LoadedObject<Trigger>> getTriggers() {
		return triggers;
	}

	public void registerIngredient(String id, Function<MMOLineConfig, Ingredient> function, ConditionalDisplay display, Predicate<NBTItem> check, Function<NBTItem, String> read) {
		ingredients.add(0, new IngredientType(id, function, display, check, read));
	}

	public void registerCondition(String id, Function<MMOLineConfig, Condition> function, ConditionalDisplay display) {
		conditions.add(new LoadedObject<>(id, function, display));
	}

	public void registerTrigger(String id, Function<MMOLineConfig, Trigger> function) {
		triggers.add(new LoadedObject<>(id, function, null));
	}

	public void registerStation(CraftingStation station) {
		stations.put(station.getId(), station);
	}

	public Collection<CraftingStation> getAll() {
		return stations.values();
	}

	public class LoadedObject<C> {
		private final String id;
		private final Function<MMOLineConfig, C> function;

		private ConditionalDisplay display;

		public LoadedObject(String id, Function<MMOLineConfig, C> function, ConditionalDisplay display) {
			this.id = id;
			this.function = function;
			this.display = display;
		}

		public String getId() {
			return id;
		}

		public void setDisplay(ConditionalDisplay display) {
			this.display = display;
		}

		public boolean hasDisplay() {
			return display != null;
		}

		public ConditionalDisplay getDisplay() {
			return display;
		}

		public C load(MMOLineConfig config) {
			return function.apply(config);
		}
	}

	public class IngredientType extends LoadedObject<Ingredient> {
		private final Predicate<NBTItem> check;
		private final Function<NBTItem, String> read;

		public IngredientType(String id, Function<MMOLineConfig, Ingredient> function, ConditionalDisplay display, Predicate<NBTItem> check, Function<NBTItem, String> read) {
			super(id, function, display);

			this.check = check;
			this.read = read;
		}

		/*
		 * returns true if the checked item can be handled by this ingredient
		 */
		public boolean check(NBTItem item) {
			return check.test(item);
		}

		/*
		 * reads the ingredient read from an NBTItem
		 */
		public String readKey(NBTItem item) {
			return read.apply(item);
		}
	}
}
