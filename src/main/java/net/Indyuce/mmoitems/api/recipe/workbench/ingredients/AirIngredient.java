package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.inventory.ItemStack;

public class AirIngredient extends WorkbenchIngredient {
	@Override
	public boolean matches(ItemStack stack) {
		if(stack == null) return true;
		else return false;
	}
	
	@Override
	public boolean matchStack(ItemStack stack) {
		return true;
	}
}
