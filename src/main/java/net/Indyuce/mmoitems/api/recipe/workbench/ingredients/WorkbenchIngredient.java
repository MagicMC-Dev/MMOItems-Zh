package net.Indyuce.mmoitems.api.recipe.workbench.ingredients;

import org.bukkit.inventory.ItemStack;

import net.mmogroup.mmolib.api.item.NBTItem;

public abstract class WorkbenchIngredient {
	private int amount;
	
	public void setAmount(int value) {
		amount = value;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public static WorkbenchIngredient getAutomatically(ItemStack stack) {
		WorkbenchIngredient ingredient;
		NBTItem nbt = NBTItem.get(stack);
		if(nbt.hasType()) ingredient = new MMOIngredient(nbt.getType(), nbt.getString("MMOITEMS_ITEM_ID"));
		else ingredient = new VanillaIngredient(stack.getType());
		ingredient.setAmount(stack.getAmount());
		return ingredient;
	}
	
	public boolean matches(ItemStack stack) {
		if(stack == null) return false;
		if(stack.getAmount() < amount) return false;
		else return matchStack(stack);
	}
	
	public abstract boolean matchStack(ItemStack stack);
}
