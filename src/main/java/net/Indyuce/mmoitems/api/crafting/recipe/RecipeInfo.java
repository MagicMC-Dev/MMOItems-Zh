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

public class RecipeInfo {
	private final Recipe recipe;

	private final Set<CheckedCondition> conditions = new LinkedHashSet<>();
	private final Set<CheckedIngredient> ingredients = new LinkedHashSet<>();

	private boolean ingredientsHad = true, conditionsMet = true;

	/*
	 * once the recipe is loaded, this class is used to reduce checkups for
	 * ingredients and conditions
	 */
	public RecipeInfo(Recipe recipe, PlayerData data, IngredientInventory inv) {
		this.recipe = recipe;

		for (Ingredient ingredient : recipe.getIngredients()) {
			CheckedIngredient info = ingredient.newIngredientInfo(inv);
			ingredients.add(info);
			if (!info.isHad()) {
				ingredientsHad = false;
				break;
			}
		}

		for (Condition condition : recipe.getConditions()) {
			CheckedCondition info = condition.newConditionInfo(data);
			conditions.add(info);
			if (!info.isMet()) {
				conditionsMet = false;
				break;
			}
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
		return conditions.stream().filter(condition -> condition.getCondition().getDisplay() != null)
				.collect(Collectors.toSet());
	}

	public Set<CheckedIngredient> getIngredients() {
		return ingredients;
	}
}
