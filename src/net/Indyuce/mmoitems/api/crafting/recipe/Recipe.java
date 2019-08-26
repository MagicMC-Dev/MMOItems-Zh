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

public abstract class Recipe {
	private final String id;
	private Map<RecipeOption, Boolean> options = new HashMap<>();

	private Set<Ingredient> ingredients = new LinkedHashSet<>();
	private Set<Condition> conditions = new LinkedHashSet<>();
	private Set<Trigger> triggers = new LinkedHashSet<>();

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
		for (String format : config.getStringList("ingredients")) {
			Ingredient ingredient = MMOItems.plugin.getCrafting().getIngredient(format);
			Validate.notNull(ingredient, id + ": Could not load ingredient '" + format + "'");
			ingredients.add(ingredient);
		}

		Validate.notEmpty(ingredients, id + ": Ingredients must not be empty");

		/*
		 * load conditions
		 */
		for (String format : config.getStringList("conditions")) {
			Condition condition = MMOItems.plugin.getCrafting().getCondition(format);
			Validate.notNull(condition, id + ": Could not load condition '" + format + "'");
			conditions.add(condition);
		}

		/*
		 * load triggers
		 */
		for (String format : config.getStringList("triggers")) {
			Trigger trigger = MMOItems.plugin.getCrafting().getTrigger(format);
			Validate.notNull(trigger, id + ": Could not load trigger '" + format + "'");
			triggers.add(trigger);
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
		HIDE_WHEN_LOCKED;

		private boolean def;

		private RecipeOption() {
			this(false);
		}

		private RecipeOption(boolean def) {
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
