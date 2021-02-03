package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import io.lumine.mythic.lib.api.item.NBTItem;

public class VanillaIngredient extends WorkbenchIngredient {
	private final Material material;

	public VanillaIngredient(Material material, int amount) {
		super(amount);

		this.material = material;
	}

	@Override
	public boolean corresponds(ItemStack stack) {
		return !NBTItem.get(stack).hasType() && stack.getType() == material;
	}

	@Override
	public ItemStack generateItem() {
		return new ItemStack(material);
	}

	@Override
	public RecipeChoice toBukkit() {
		return new RecipeChoice.MaterialChoice(material);
	}
}
