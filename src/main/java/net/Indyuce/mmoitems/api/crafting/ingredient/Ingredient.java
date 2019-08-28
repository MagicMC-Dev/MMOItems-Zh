package net.Indyuce.mmoitems.api.crafting.ingredient;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.item.NBTItem;

public abstract class Ingredient {
	private final String id;
	private String key, name;
	private ConditionalDisplay display;
	private int amount;

	public Ingredient(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public ConditionalDisplay getDisplay() {
		return display;
	}

	/*
	 * if the condition has no default display, then the condition CANNOT
	 * display in the lore at all.
	 */
	public boolean hasDisplay() {
		return display != null;
	}

	protected void setKey(String key) {
		this.key = getId() + ":" + key;
	}

	protected void setAmount(int amount) {
		this.amount = amount;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setDisplay(ConditionalDisplay display) {
		this.display = display;
	}

	/*
	 * used when loading recipes.
	 */
	public abstract Ingredient load(String[] args);

	public int getAmount() {
		return amount;
	}

	public String getName() {
		return name;
	}

	/*
	 * apply specific placeholders to display the ingredient in the item lore.
	 */
	public abstract String formatDisplay(String string);

	/*
	 * check if an itemstack can be read by this ingredient.
	 */
	public abstract boolean isValid(NBTItem item);

	public abstract String readKey(NBTItem item);
	
	public abstract ItemStack generateItemStack();

	public IngredientInfo newIngredientInfo(IngredientInventory inv) {
		return new IngredientInfo(this, inv.getIngredient(this));
	}

	public class IngredientInfo {
		private final Ingredient inventory;
		private final PlayerIngredient player;

		private IngredientInfo(Ingredient inventory, PlayerIngredient player) {
			this.inventory = inventory;
			this.player = player;
		}

		public boolean isHad() {
			return player != null && player.getAmount() >= inventory.getAmount();
		}

		public Ingredient getIngredient() {
			return inventory;
		}

		public PlayerIngredient getPlayerIngredient() {
			return player;
		}
	}
}
