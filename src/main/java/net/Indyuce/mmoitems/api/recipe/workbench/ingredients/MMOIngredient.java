package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOIngredient extends WorkbenchIngredient {
	private final Type type;
	private final String id;

	public MMOIngredient(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public boolean matchStack(ItemStack stack) {
		NBTItem nbt = NBTItem.get(stack);
		if (!nbt.hasType())
			return false;
		return nbt.getType().equals(type) &&
			nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(id);
	}

}
