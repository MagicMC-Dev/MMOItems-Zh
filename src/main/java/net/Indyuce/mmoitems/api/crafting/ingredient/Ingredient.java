package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.IngredientLookupMode;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	/**
	 * @return The ingredient key which is used internally by MMOItems to check
	 *         if two ingredients are of the same nature.
	 */
	public abstract String getKey();

	/**
	 * Apply specific placeholders to display the ingredient in the item lore.
	 * 
	 * @param  s String with unparsed placeholders
	 * @return        String with parsed placeholders
	 */
	public abstract String formatDisplay(String s);

	/**
	 * When the player right-clicks one of the items in a station, they can
	 * preview the stats if itself and the components it is made of. This
	 * is called to displace those preview elements.
	 *
	 * @param player Player looking at the recipe
	 *
	 * @return The ItemStack to display to the player
	 */
	@NotNull public abstract ItemStack generateItemStack(@NotNull RPGPlayer player);

	public CheckedIngredient evaluateIngredient(@NotNull IngredientInventory inv) {
		return new CheckedIngredient(this, inv.getIngredient(this, IngredientLookupMode.BASIC));
	}

	public static class CheckedIngredient {
		@NotNull private final Ingredient ingredient;
		@Nullable private final PlayerIngredient found;

		/**
		 * Instantiated everytime an ingredient is evaluated for a player when a
		 * CheckedRecipe is being created (when a player is opening a crafting
		 * station). This helps greatly reducing ingredient checkups by caching
		 * the items the plugin will need to take off the player's ingredient
		 * 
		 * @param ingredient The ingredient being evaluated
		 * @param found      The corresponding ingredient found in the player's
		 *                   ingredient
		 */
		private CheckedIngredient(@NotNull Ingredient ingredient, @Nullable PlayerIngredient found) {
			this.ingredient = ingredient;
			this.found = found;
		}

		/**
		 * @return If the player has enough of the specific item or not
		 */
		public boolean isHad() { return found != null && found.getAmount() >= ingredient.getAmount(); }

		@NotNull public Ingredient getIngredient() { return ingredient; }

		@Nullable public PlayerIngredient getPlayerIngredient() { return found; }

		@NotNull public String format() { return ingredient.formatDisplay(isHad() ? ingredient.getDisplay().getPositive() : ingredient.getDisplay().getNegative()); }
	}
}
