package net.Indyuce.mmoitems.api.crafting.recipe;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class Recipe {
	private final String id;
	private final Map<RecipeOption, Boolean> options = new HashMap<>();

	private final Set<Ingredient> ingredients = new LinkedHashSet<>();
	private final Set<Condition> conditions = new LinkedHashSet<>();
	private final Set<Trigger> triggers = new LinkedHashSet<>();

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

		Validate.notEmpty(ingredients, id + ": Ingredients must not be empty");

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

	public boolean getOption(RecipeOption option) {
		return options.containsKey(option) ? options.get(option) : option.getDefault();
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

	public RecipeInfo getRecipeInfo(PlayerData data, IngredientInventory inv) {
		return new RecipeInfo(this, data, inv);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Recipe && ((Recipe) obj).id.equals(id);
	}

	/*
	 * when the recipe is claimed once in the crafting queue. if it returns
	 * true, the conditions are applied and the ingredients are taken off the
	 * player's inventory
	 */
	public abstract void whenUsed(PlayerData data, IngredientInventory inv, RecipeInfo recipe, CraftingStation station);

	/*
	 * extra conditions when trying to use the recipe.
	 */
	public abstract boolean canUse(PlayerData data, IngredientInventory inv, RecipeInfo recipe, CraftingStation station);

	public abstract ItemStack display(RecipeInfo recipe);

	public enum RecipeOption {
		HIDE_WHEN_LOCKED,
		OUTPUT_ITEM,
		SILENT_CRAFT;

		private final boolean def;

		private RecipeOption() {
			// this stores the defaults of the enums
			HashMap<String, Boolean> defaultMap = new HashMap<String, Boolean>() {{
				put("HIDE_WHEN_LOCKED", false);
				put("OUTPUT_ITEM", true);
				put("SILENT_CRAFT", false);
			}};
			this.def = defaultMap.get(this.toString());
		}

		public boolean getDefault() {
			return def;
		}

		public String getConfigPath() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
