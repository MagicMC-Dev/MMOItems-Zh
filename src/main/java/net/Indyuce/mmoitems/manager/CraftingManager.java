package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
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
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;

public class CraftingManager {

	/*
	 * a list is used here because the order in which the ingredients are called
	 * to check every item in the player inventory matters. a vanilla item is
	 * always recognized so it must be called after the mmoitem ingredient
	 */
	private List<Ingredient> ingredients = new ArrayList<>();
	private Set<Condition> conditions = new HashSet<>();
	private Set<Trigger> triggers = new HashSet<>();

	private Map<String, CraftingStation> stations = new HashMap<>();

	public CraftingManager() {
		registerCondition(new LevelCondition());
		registerCondition(new PermissionCondition());
		registerCondition(new ManaCondition());
		registerCondition(new StaminaCondition());
		registerCondition(new FoodCondition());
		registerCondition(new ClassCondition());
		registerCondition(new IngredientCondition());

		registerIngredient(new MMOItemIngredient());
		registerIngredient(new VanillaIngredient());
	}

	public void reload() {
		stations.clear();

		ConfigFile language = new ConfigFile("/language", "crafting-stations");

		for (Condition condition : getConditions())
			if (condition.displays()) {
				String path = "condition." + condition.getId();
				if (!language.getConfig().contains(path)) {
					language.getConfig().createSection(path);
					condition.getDisplay().setup(language.getConfig().getConfigurationSection(path));
				}

				condition.getDisplay().load(language.getConfig().getConfigurationSection(path));
			}

		for (Ingredient ingredient : getIngredients()) {
			String path = "ingredient." + ingredient.getId();
			if (!language.getConfig().contains(path)) {
				language.getConfig().createSection(path);
				ingredient.getDisplay().setup(language.getConfig().getConfigurationSection(path));
			}

			ingredient.getDisplay().load(language.getConfig().getConfigurationSection(path));
		}

		language.save();

		for (File file : new File(MMOItems.plugin.getDataFolder() + "/crafting-stations").listFiles())
			try {
				CraftingStation station = new CraftingStation(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file));
				stations.put(station.getId(), station);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load recipe station " + file.getName());
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

	public CraftingStation getStation(String id) {
		return stations.get(id);
	}

	public Trigger getTrigger(String format) {
		String type = format.split("\\ ")[0];

		for (Trigger trigger : triggers)
			if (trigger.getId().equalsIgnoreCase(type))
				return trigger.load(format.replaceFirst(type + " ", "").split("\\ "));

		return null;
	}

	public Condition getCondition(String format) {
		String type = format.split("\\ ")[0];

		for (Condition condition : conditions)
			if (condition.getId().equalsIgnoreCase(type))
				return condition.load(format.replaceFirst(type + " ", "").split("\\ "));

		return null;
	}

	public Ingredient getIngredient(String format) {
		String type = format.split("\\ ")[0];

		for (Ingredient ingredient : ingredients)
			if (ingredient.getId().equalsIgnoreCase(type))
				return ingredient.load(format.replaceFirst(type + " ", "").split("\\ "));

		return null;
	}

	public void registerCondition(Condition condition) {
		conditions.add(condition);
	}

	public void registerIngredient(Ingredient ingredient) {
		ingredients.add(ingredient);
	}

	public void registerTrigger(Trigger trigger) {
		triggers.add(trigger);
	}

	public void registerStation(CraftingStation station) {
		stations.put(station.getId(), station);
	}

	public Collection<CraftingStation> getAll() {
		return stations.values();
	}

	public Set<Condition> getConditions() {
		return conditions;
	}

	public Set<Trigger> getTriggers() {
		return triggers;
	}

	public List<Ingredient> getIngredients() {
		return ingredients;
	}
}
