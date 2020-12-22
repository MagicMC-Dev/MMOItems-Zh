package net.Indyuce.mmoitems.api.crafting.recipe;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.condition.Condition.CheckedCondition;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CheckedRecipe {
	private final Recipe recipe;

	private final Set<CheckedCondition> conditions = new LinkedHashSet<>();
	private final Set<CheckedIngredient> ingredients = new LinkedHashSet<>();

	private boolean ingredientsHad = true, conditionsMet = true;

	/**
	 * An instance of CheckedRecipe is created, for every recipe in a crafting
	 * station whenever a player opens a crafting station. This class calculates
	 * the ingredients the player is missing and the conditions which he does
	 * not meet. It is used to display the missing ingredients on the GUI recipe
	 * items.
	 * 
	 * @param recipe The corresponding crafting recipe
	 * @param data   The player opening the crafting station
	 * @param inv    The player's ingredients
	 */
	public CheckedRecipe(Recipe recipe, PlayerData data, IngredientInventory inv) {
		this.recipe = recipe;

		for (Ingredient ingredient : recipe.getIngredients()) {
			CheckedIngredient info = ingredient.evaluateIngredient(inv);
			ingredients.add(info);
			if (!info.isHad())
				ingredientsHad = false;
		}

		for (Condition condition : recipe.getConditions()) {
			CheckedCondition info = condition.evaluateCondition(data);
			conditions.add(info);
			if (!info.isMet())
				conditionsMet = false;
		}
	}

	public Recipe getRecipe() {
		return recipe;
	}

	@Deprecated
	public boolean isUnlocked() {
		return ingredientsHad && conditionsMet;
	}

	public boolean areConditionsMet() {
		return conditionsMet;
	}

	public boolean allIngredientsHad() {
		return ingredientsHad;
	}

	public ItemStack display() {
		return recipe.display(this);
	}

	public Set<CheckedCondition> getConditions() {
		return conditions;
	}

	public Set<CheckedCondition> getDisplayableConditions() {
		return conditions.stream().filter(condition -> condition.getCondition().getDisplay() != null).collect(Collectors.toSet());
	}

	public Set<CheckedIngredient> getIngredients() {
		return ingredients;
	}
}
