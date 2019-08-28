package net.Indyuce.mmoitems.api.crafting.recipe;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.condition.Condition.ConditionInfo;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient.IngredientInfo;
import net.Indyuce.mmoitems.api.player.PlayerData;

/*
 * once the recipe is loaded, this class is used to reduce checkups for
 * ingredients and conditions
 */
public class RecipeInfo {
	private final Recipe recipe;
	
	private Set<ConditionInfo> conditions = new LinkedHashSet<>();
	private Set<IngredientInfo> ingredients = new LinkedHashSet<>();

	private boolean ingredientsHad = true, conditionsMet = true;

	public RecipeInfo(Recipe recipe, PlayerData data, IngredientInventory inv) {
		this.recipe = recipe;
		
		for (Condition condition : recipe.getConditions()) {
			ConditionInfo info = condition.newConditionInfo(data);
			conditions.add(info);
			if (!info.isMet())
				conditionsMet = false;
		}

		for (Ingredient ingredient : recipe.getIngredients()) {
			IngredientInfo info = ingredient.newIngredientInfo(inv);
			ingredients.add(info);
			if (!info.isHad())
				ingredientsHad = false;
		}
	}

	public Recipe getRecipe() {
		return recipe;
	}

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

	public ConditionInfo getCondition(String format) {
		for (ConditionInfo condition : conditions)
			if (condition.getCondition().getId().equals(format))
				return condition;
		return null;
	}

	public Set<ConditionInfo> getConditions() {
		return conditions;
	}

	public Set<IngredientInfo> getIngredients() {
		return ingredients;
	}
}
