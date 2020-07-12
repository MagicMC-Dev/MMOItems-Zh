package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.mmogroup.mmolib.api.item.NBTItem;

public class VanillaIngredient extends WorkbenchIngredient {
	private final Material mat;
	
	public VanillaIngredient(Material mat) {
		this.mat = mat;
	}

	@Override
	public boolean matchStack(ItemStack stack) {
		if(NBTItem.get(stack).hasType()) return false;
		return stack.getType() == mat;
	}
}
