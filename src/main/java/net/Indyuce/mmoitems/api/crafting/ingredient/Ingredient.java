package net.Indyuce.mmoitems.api.crafting.ingredient;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.IngredientLookupMode;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.inventory.ItemStack;

public abstract class Ingredient {
	private final String id;
	private final int amount;

	public Ingredient(String id, MMOLineConfig config) {
		this(id, config.getInt("amount", 1));
	}

	public Ingredient(String id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	public String getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	/*
	 * shortcut to RecipeManager map lookup, may throw a stream lookup error if
	 * the condition has not been registered.
	 */
	public ConditionalDisplay getDisplay() {
		return MMOItems.plugin.getCrafting().getIngredients().stream().filter(type -> type.getId().equals(id)).findAny().orElse(null).getDisplay();
	}

	/*
	 * ingredient key is used internally by plugin to check if two ingredients
	 * are of the same nature. name is the actual piece of string displayed
	 */
	public abstract String getKey();

	/*
	 * apply specific placeholders to display the ingredient in the item lore.
	 */
	public abstract String formatLoreDisplay(String string);

	public abstract ItemStack generateItemStack(RPGPlayer player);

	public CheckedIngredient newIngredientInfo(IngredientInventory inv) {
		return new CheckedIngredient(this, inv.getIngredient(this, IngredientLookupMode.BASIC));
	}

	/*
	 * used to reduce calculations when the player has opened the crafting
	 * station. ingredientInfo instances must be updated everytime the player's
	 * inventory updates.
	 */
	public static class CheckedIngredient {
		private final Ingredient inventory;
		private final PlayerIngredient found;

		private CheckedIngredient(Ingredient inventory, PlayerIngredient found) {
			this.inventory = inventory;
			this.found = found;
		}

		/*
		 * checks if the player has a specific item or not
		 */
		public boolean isHad() {
			return found != null && found.getAmount() >= inventory.getAmount();
		}

		public Ingredient getIngredient() {
			return inventory;
		}

		public PlayerIngredient getPlayerIngredient() {
			return found;
		}

		public String format() {
			return inventory.formatLoreDisplay(isHad() ? inventory.getDisplay().getPositive() : inventory.getDisplay().getNegative());
		}
	}
}
