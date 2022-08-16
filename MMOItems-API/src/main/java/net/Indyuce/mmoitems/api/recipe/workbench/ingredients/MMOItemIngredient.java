package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.api.item.NBTItem;

public class MMOItemIngredient extends WorkbenchIngredient {
	private final Type type;
	private final String id;

	public MMOItemIngredient(Type type, String id, int amount) {
		super(amount);

		this.type = type;
		this.id = id;
	}

	@Override
	public boolean corresponds(ItemStack stack) {
		NBTItem nbt = NBTItem.get(stack);
		return type.equals(Type.get(nbt.getType())) && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(id);
	}

	@Override
	public ItemStack generateItem() {
		return MMOItems.plugin.getItem(type, id);
	}

	@SuppressWarnings("deprecation")
	@Override
	public RecipeChoice toBukkit() {
		return new RecipeChoice.ExactChoice(generateItem());
	}
}
