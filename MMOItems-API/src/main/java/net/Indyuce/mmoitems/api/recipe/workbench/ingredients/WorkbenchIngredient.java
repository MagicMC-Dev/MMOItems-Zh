package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public abstract class WorkbenchIngredient {
	private final int amount;

	public WorkbenchIngredient(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public boolean matches(ItemStack stack) {
		return stack != null && stack.getAmount() >= amount && corresponds(stack);
	}
	
	public abstract RecipeChoice toBukkit();
	
	public abstract ItemStack generateItem();

	protected abstract boolean corresponds(ItemStack stack);
}
