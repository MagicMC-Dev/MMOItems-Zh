package net.Indyuce.mmoitems.api.recipe;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMORecipeChoice {
	private final ItemStack item;
	private final int amount;
	private final boolean isVanilla;

	public MMORecipeChoice(String input) {
		item = MMOItems.plugin.getRecipes().parseStack(input);

		if (item != null) {
			isVanilla = !NBTItem.get(item).hasType();
			amount = item.getAmount();
		} else {
			amount = 1;
			isVanilla = true;
		}
	}

	public boolean isValid() {
		return item != null;
	}

	public boolean isVanilla() {
		return isVanilla;
	}

	public int getAmount() {
		return amount;
	}

	public ItemStack getItem() {
		return item;
	}
}
