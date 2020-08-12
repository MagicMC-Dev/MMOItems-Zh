package net.Indyuce.mmoitems.api.recipe;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMORecipeChoice {
	private final ItemStack item;
	private final int amount;
	private final boolean isVanilla;

	public MMORecipeChoice(String input) {
		item = MMOItems.plugin.getRecipes().parseStack(input);
		Validate.notNull(item, "Could not parse recipe choice");

		isVanilla = !NBTItem.get(item).hasType();
		amount = item.getAmount();
	}

	public ItemStack getItem() {
		return item;
	}

	public int getAmount() {
		return amount;
	}

	public boolean isVanilla() {
		return isVanilla;
	}
}
