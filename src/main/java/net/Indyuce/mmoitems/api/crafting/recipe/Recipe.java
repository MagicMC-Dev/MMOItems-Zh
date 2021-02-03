package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class Recipe {
	private final String id;
	private final Map<RecipeOption, Boolean> options = new HashMap<>();

	private final Set<Ingredient> ingredients = new LinkedHashSet<>();
	private final Set<Condition> conditions = new LinkedHashSet<>();
	private final Set<Trigger> triggers = new LinkedHashSet<>();

	/**
	 * Stores the information about a specific recipe setup in a crafting
	 * station. When a player opens a crafting station GUI, a CheckedRecipe
	 * instance is created to evaluate the conditions which are not met/the
	 * ingredients he is missing.
	 * 
	 * @param config Config section to load data from
	 */
	public Recipe(ConfigurationSection config) {
		this(config.getName());

		/*
		 * load recipe options
		 */
		if (config.contains("options"))
			for (RecipeOption option : RecipeOption.values())
				if (config.getConfigurationSection("options").contains(option.getConfigPath()))
					options.put(option, config.getBoolean("options." + option.getConfigPath()));

		/*
		 * load ingredients
		 */
		for (String format : config.getStringList("ingredients"))
			try {
				Ingredient ingredient = MMOItems.plugin.getCrafting().getIngredient(new MMOLineConfig(format));
				Validate.notNull(ingredient, "Could not match ingredient");
				ingredients.add(ingredient);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Could not load ingredient '" + format + "': " + exception.getMessage());
			}

		/*
		 * load conditions
		 */
		for (String format : config.getStringList("conditions"))
			try {
				Condition condition = MMOItems.plugin.getCrafting().getCondition(new MMOLineConfig(format));
				Validate.notNull(condition, "Could not match condition");
				conditions.add(condition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Could not load condition '" + format + "': " + exception.getMessage());
			}

		if (conditions.isEmpty() && ingredients.isEmpty()) {
			throw new IllegalArgumentException("No conditions or ingredients set.");
		}

		/*
		 * load triggers
		 */
		for (String format : config.getStringList("triggers"))
			try {
				Trigger trigger = MMOItems.plugin.getCrafting().getTrigger(new MMOLineConfig(format));
				Validate.notNull(trigger, "Could not match trigger");
				triggers.add(trigger);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Could not load trigger '" + format + "': " + exception.getMessage());
			}
	}

	private Recipe(String id) {
		Validate.notNull(id, "Recipe ID must not be null");

		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Set<Ingredient> getIngredients() {
		return ingredients;
	}

	public Set<Condition> getConditions() {
		return conditions;
	}

	public Set<Trigger> getTriggers() {
		return triggers;
	}

	public Condition getCondition(String format) {
		for (Condition condition : conditions)
			if (condition.getId().equals(format))
				return condition;
		return null;
	}

	public boolean hasOption(RecipeOption option) {
		return options.getOrDefault(option, option.getDefault());
	}

	public void addIngredient(Ingredient ingredient) {
		ingredients.add(ingredient);
	}

	public void registerCondition(Condition condition) {
		conditions.add(condition);
	}

	public void setOption(RecipeOption option, boolean value) {
		options.put(option, value);
	}

	public CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv) {
		return new CheckedRecipe(this, data, inv);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Recipe && ((Recipe) obj).id.equals(id);
	}

	/**
	 * Called when all the recipe conditions are to true and when the player
	 * eventually starts crafting OR when the player claims the item in the
	 * crafting queue once the delay is over.
	 * 
	 * @param data    Player crafting the item
	 * @param inv     The player's ingredients
	 * @param recipe  The recipe used to craft the item
	 * @param station The station used to craft the item
	 */
	public abstract void whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station);

	/**
	 * Applies extra conditions when a player has just clicked on a recipe item
	 * in the GUI. This method is called after checking for the recipe
	 * conditions and ingredients.
	 * 
	 * @param  data    The player crafting the item
	 * @param  inv     The player's ingredients
	 * @param  recipe  The recipe used to craft the item
	 * @param  station The station used to craft the item
	 * @return         If the player can use the recipe
	 */
	public abstract boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station);

	public abstract ItemStack display(CheckedRecipe recipe);

	public enum RecipeOption {

		/**
		 * Hide the crafting recipe when one of the condition is not met
		 */
		HIDE_WHEN_LOCKED(false),

		/**
		 * Hide the crafting recipe when the player does not have all the
		 * ingredients
		 */
		HIDE_WHEN_NO_INGREDIENTS(false),

		/**
		 * If set to false (default is true), no output item will be given to
		 * the player crafting the item. That option is made to have recipes
		 * which entirely rely on triggers.
		 */
		OUTPUT_ITEM(true),

		/**
		 * Disables crafting sound
		 */
		SILENT_CRAFT(false);

		private final boolean def;

		RecipeOption(boolean def) {
			this.def = def;
		}

		public boolean getDefault() {
			return def;
		}

		public String getConfigPath() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
