package net.Indyuce.mmoitems.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.manager.CraftingManager.IngredientType;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class IngredientInventory {

	/*
	 * ingredients in the map are not stored using class instances as keys but
	 * using a unique string which is an unique ingredient identifier. allows
	 * for faster map check ups and fixes the >64 amount and ingredient
	 * splitting glitches
	 */
	private final Map<String, PlayerIngredient> ingredients = new HashMap<>();

	public IngredientInventory(Player player) {
		this(player.getInventory());
	}

	public IngredientInventory(Inventory inv) {
		loop: for (ItemStack item : inv.getContents())
			if (item != null && item.getType() != Material.AIR) {
				NBTItem nbt = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item);
				for (IngredientType ingredient : MMOItems.plugin.getCrafting().getIngredients()) {
					if (ingredient.check(nbt)) {
						addIngredient(nbt, ingredient);
						continue loop;
					}
				}
			}
	}

	public void addIngredient(NBTItem item, IngredientType ingredient) {
		String key = ingredient.getId() + ":" + ingredient.readKey(item);

		if (ingredients.containsKey(key))

			/*
			 * add to current ingredient a specific amount.
			 */
			ingredients.get(key).add(item.getItem());
		else

			/*
			 * load an item stack and turn it into an ingredient which makes
			 * checking ingredients later much faster.
			 */
			ingredients.put(key, new PlayerIngredient(item.getItem()));
	}

	public PlayerIngredient getIngredient(Ingredient ingredient, IngredientLookupMode lookupMode) {
		String key = ingredient.getKey();

		for (String invKey : ingredients.keySet()) {
			String ingredientKey = lookupMode == IngredientLookupMode.IGNORE_ITEM_LEVEL ? invKey.replaceFirst("-\\d*_", "_") : invKey;
			if (ingredientKey.equals(key))
				return ingredients.get(invKey);
		}

		return null;
	}

	@Deprecated
	public boolean hasIngredient(Ingredient ingredient) {
		PlayerIngredient found = getIngredient(ingredient, IngredientLookupMode.IGNORE_ITEM_LEVEL);
		return found != null && found.getAmount() >= ingredient.getAmount();
	}

	public class PlayerIngredient {

		/*
		 * stores items which correspond to a specific ingredient. when the
		 * ingredient is taken off the player inventory, the itemstack amounts
		 * get lowered. these POINT towards the player inventory itemStacks.
		 */
		private final List<ItemStack> items = new ArrayList<>();

		public PlayerIngredient(ItemStack item) {
			items.add(item);
		}

		public int getAmount() {
			int t = 0;
			for (ItemStack item : items)
				t += item.getAmount();
			return t;
		}

		public void add(ItemStack item) {
			items.add(item);
		}

		// used for upgrading recipes
		public ItemStack getFirstItem() {
			return items.get(0);
		}

		/*
		 * algorythm which takes away a certain amount of items. used to consume
		 * ingredients when using recipes
		 */
		public void reduceItem(int amount) {

			Iterator<ItemStack> iterator = items.iterator();
			while (iterator.hasNext() && amount > 0) {
				ItemStack item = iterator.next();

				// remove itemStack from list if amount <= 0
				if (item.getAmount() < 1) {
					iterator.remove();
					continue;
				}

				// amount of items it can take from this particular ItemStack
				int take = Math.min(item.getAmount(), amount);

				amount -= take;
				item.setAmount(item.getAmount() - take);
			}
		}
	}

	/*
	 * could use a boolean because there are only two states possible, but makes
	 * things clearer.
	 */
	public enum IngredientLookupMode {

		/*
		 * item level must be ignored when the player is using an upgrading
		 * recipe, otherwise recipe cannot identify right item
		 */
		IGNORE_ITEM_LEVEL,

		/*
		 * scans ingredient inventory considering item levels
		 */
		BASIC;
	}
}
