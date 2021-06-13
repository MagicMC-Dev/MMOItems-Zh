package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.manager.CraftingManager.IngredientType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IngredientInventory {

	/*
	 * ingredients in the map are not stored using class instances as keys but
	 * using a unique string which is an unique ingredient identifier. allows
	 * for faster map check ups and fixes the >64 amount and ingredient
	 * splitting glitches
	 */
	private final Map<String, PlayerIngredient> ingredients = new HashMap<>();

	public IngredientInventory(Player player) { this(player.getInventory()); }

	public IngredientInventory(Inventory inv) {
		loop: for (ItemStack item : inv.getContents())
			if (item != null && item.getType() != Material.AIR) {
				NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item);
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

	@Nullable
	public PlayerIngredient getIngredient(@NotNull Ingredient ingredient, @NotNull IngredientLookupMode lookupMode) {
		String key = ingredient.getKey();

		// Find level
		QuickNumberRange lvl = null;
		int dsh = key.indexOf('-');
		if (dsh > 0) {

			// Get lvl
			String itemCrop = key.substring(dsh + 1);
			String itemLevel = itemCrop.substring(0, itemCrop.indexOf('_'));
			lvl = QuickNumberRange.getFromString(itemLevel);
			key = key.substring(0, dsh) + key.substring(dsh + 1 + itemLevel.length()); }

		// Remove lvl
		//ING//MMOItems.log("\u00a7a>\u00a78>\u00a77 Reading ingredient\u00a7a " + key + "\u00a77 (of level \u00a7a" + lvl + "\u00a77)");

		for (String invKey : ingredients.keySet()) {

			int dash = invKey.indexOf('-');
			Integer itemLvl = null;
			String ingredientKey = invKey;
			if (dash > 0) {

				// Get lvl
				String itemCrop = invKey.substring(dash + 1);
				String itemLevel = itemCrop.substring(0, itemCrop.indexOf('_'));
				itemLvl = SilentNumbers.IntegerParse(itemLevel);
				ingredientKey = invKey.substring(0, dash) + invKey.substring(dash + 1 + itemLevel.length()); }


			// Compare removing level
			//ING//MMOItems.log(" \u00a7a>\u00a77 Comparing to \u00a7b" + invKey + "\u00a77 (\u00a73" + ingredientKey + "\u00a77)");

			if (ingredientKey.equals(key)) {

				// Get level
				boolean levelMet = true;
				if (lookupMode != IngredientLookupMode.IGNORE_ITEM_LEVEL && lvl != null) {

					// Parse
					if (itemLvl == null) { itemLvl = 0; }
					levelMet = lvl.inRange(itemLvl);
					//ING//MMOItems.log(" \u00a7a>\u00a77 Was level \u00a7e" + invKey + "\u00a77 (\u00a76" + levelMet + "\u00a77)");
					}

				if (levelMet) { return ingredients.get(invKey); }
			}
		}

		return null;
	}

	@Deprecated
	public boolean hasIngredient(Ingredient ingredient) {
		PlayerIngredient found = getIngredient(ingredient, IngredientLookupMode.IGNORE_ITEM_LEVEL);
		return found != null && found.getAmount() >= ingredient.getAmount();
	}

	public static class PlayerIngredient {

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
		BASIC
	}
}
