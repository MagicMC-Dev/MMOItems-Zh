package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

@Deprecated
public class AirIngredient extends WorkbenchIngredient {
	public AirIngredient() {
		super(0);
	}

	@Override
	public boolean matches(ItemStack stack) {
		return stack == null || stack.getType() == Material.AIR;
	}

	@Override
	public boolean corresponds(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack generateItem() {
		return new ItemStack(Material.AIR);
	}

	@Override
	public RecipeChoice toBukkit() {
		return new RecipeChoice.MaterialChoice(Material.AIR);
	}
}
