package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An ingredient from a crafting station recipe.
 * <p>
 * See {@link PlayerIngredient} for more information.
 */
public abstract class Ingredient<C extends PlayerIngredient> {
	private final String id;
	private int amount;

	public Ingredient(String id, MMOLineConfig config) {
		this(id, config.getInt("amount", 1));
	}

	public Ingredient(String id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	/**
	 * @return The ingredient type id i.e the string placed
	 * at the beginning of the line config
	 */
	public String getId() {
		return id;
	}

	public void setAmount(int amount) { this.amount = amount; }
	public int getAmount() {
		return amount;
	}

	/**
	 * Shortcut to RecipeManager map lookup, may throw a stream
	 * lookup error if the ingredient has not been registered.
	 */
	public ConditionalDisplay getDisplay() {
		return MMOItems.plugin.getCrafting().getIngredients().stream().filter(type -> type.getId().equals(id)).findAny().orElseThrow().getDisplay();
	}

	/**
	 * @return The ingredient key which is used internally by MMOItems to check
	 * if two ingredients are of the same nature.
	 * @deprecated Apart from ingredient type keys, keys are not used anymore.
	 */
	@Deprecated
	public abstract String getKey();

	/**
	 * Apply specific placeholders to display the ingredient in the item lore.
	 *
	 * @param s String with unparsed placeholders
	 * @return String with parsed placeholders
	 */
	public abstract String formatDisplay(String s);

	public abstract boolean matches(C playerIngredient);

	/**
	 * When the player right-clicks one of the items in a station, they can
	 * preview the stats if itself and the components it is made of. This
	 * is called to displace those preview elements.
	 *
	 * @param player Player looking at the recipe
	 * @return The ItemStack to display to the player
	 */
	@NotNull
	public abstract ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay);

	public CheckedIngredient evaluateIngredient(@NotNull IngredientInventory inv) {
		return inv.findMatching(this);
	}
}
