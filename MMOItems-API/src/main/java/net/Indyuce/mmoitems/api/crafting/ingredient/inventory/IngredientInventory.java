package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.IngredientType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IngredientInventory {
	private final Map<String, Set<PlayerIngredient>> ingredients = new HashMap<>();

	/**
	 * Loads all the possible crafting station ingredients from a player's inventory
	 */
	public IngredientInventory(Player player) {
		this(player.getInventory());
	}

	/**
	 * Loads all the possible crafting station ingredients from an inventory
	 */
	public IngredientInventory(Inventory inv) {
		loop:
		for (ItemStack item : inv.getContents())
			if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
				NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item);
				for (IngredientType ingredient : MMOItems.plugin.getCrafting().getIngredients()) {
					if (ingredient.check(nbt)) {
						addIngredient(nbt, ingredient);
						continue loop;
					}
				}
			}
	}

	/**
	 * Registers an ingredient.
	 *
	 * @param item       The actual item in the inventory
	 * @param ingredient The type of the ingredient added
	 */
	public void addIngredient(NBTItem item, IngredientType ingredient) {
		String key = ingredient.getId();

		// Add to existing set
		if (ingredients.containsKey(key))
			ingredients.get(key).add(ingredient.readPlayerIngredient(item));

			// Initialize
		else {
			Set<PlayerIngredient> ingredients = new HashSet<>();
			ingredients.add(ingredient.readPlayerIngredient(item));
			this.ingredients.put(key, ingredients);
		}
	}

	@Nullable
	public CheckedIngredient findMatching(@NotNull Ingredient ingredient) {
		Set<PlayerIngredient> found = new HashSet<>();
		if (!ingredients.containsKey(ingredient.getId()))
			return new CheckedIngredient(ingredient, found);

		for (PlayerIngredient checked : ingredients.get(ingredient.getId()))
			if (ingredient.matches(checked))
				found.add(checked);

		return new CheckedIngredient(ingredient, found);
	}

	/**
	 * @deprecated First use {@link #findMatching(Ingredient)} and cache its
	 * result to use the isHad() method of returned class instead.
	 */
	@Deprecated
	public boolean hasIngredient(Ingredient ingredient) {
		return findMatching(ingredient).isHad();
	}

	/**
	 * Could use a boolean because there are only two
	 * states possible, but makes things clearer.
	 *
	 * @deprecated Since 6.6 where ingredients were recoded.
	 */
	@Deprecated
	public enum IngredientLookupMode {

		/**
		 * Item level must be ignored when the player is using an upgrading
		 * recipe, otherwise recipe cannot identify right item
		 */
		IGNORE_ITEM_LEVEL,

		/**
		 * Scans ingredient inventory considering item levels
		 */
		BASIC
	}
}
