package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.List;

import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.util.message.Message;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;

public class CraftingStationPreview extends PluginInventory {
	private final CraftingStationView previous;
	private final CheckedRecipe recipe;

	private final List<ItemStack> ingredients = new ArrayList<>();

	private static final int[] slots = { 12, 13, 14, 21, 22, 23, 30, 31, 32 },
			fill = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 15, 17, 18, 19, 25, 26, 27, 29, 33, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 };

	public CraftingStationPreview(CraftingStationView previous, CheckedRecipe recipe) {
		super(previous.getPlayer());

		this.previous = previous;
		this.recipe = recipe;
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 45, Message.RECIPE_PREVIEW.formatRaw(ChatColor.RESET));
		ingredients.clear();
		for (CheckedIngredient ing : recipe.getIngredients()) {
			if (ing.getIngredient().getAmount() > 64) {
				ItemStack sample = ing.getIngredient().generateItemStack(playerData.getRPG());
				sample.setAmount(64);
				int amount = ing.getIngredient().getAmount();
				// calculate how many full stacks there are
				int stacks = amount / 64;
				// check for remainders
				if ((stacks % 64) == 0)
					// simply add the desired amount of ingredients
					for (int i = 0; i < stacks; i++)
						ingredients.add(sample.clone());
				else
					// iterate stacks + 1 for the final one
					for (int i = 0; i < (stacks + 1); i++) {
						if (i == stacks)
							sample.setAmount(amount - (stacks * 64));
						ingredients.add(sample.clone());
					}
			} else
				ingredients.add(ing.getIngredient().generateItemStack(playerData.getRPG()));
		}

		int min = (page - 1) * slots.length, max = page * slots.length;
		for (int j = min; j < max; j++) {
			if (j >= ingredients.size())
				break;
			inv.setItem(slots[j - min], ingredients.get(j));
		}

		for (int slot : fill)
			inv.setItem(slot, ConfigItems.FILL.getItem());

		if (recipe.getRecipe() instanceof CraftingRecipe) {
			ItemStack item = ((CraftingRecipe) recipe.getRecipe()).getOutput().getPreview();
			item.setAmount(((CraftingRecipe) recipe.getRecipe()).getOutput().getAmount());
			inv.setItem(16, item);
		}
		if (recipe.getRecipe() instanceof UpgradingRecipe) {
			ItemStack stack = ((UpgradingRecipe) recipe.getRecipe()).getItem().getPreview();
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(meta.getDisplayName() + ChatColor.GREEN + "+1!");
			stack.setItemMeta(meta);
			inv.setItem(16, stack);
		}

		inv.setItem(10, ConfigItems.BACK.getItem());
		inv.setItem(34, ConfigItems.CONFIRM.getItem());
		ItemStack book = recipe.display();
		book.setType(Material.KNOWLEDGE_BOOK);
		book.setAmount(1);
		ItemMeta meta = book.getItemMeta();
		List<String> newLore = meta.getLore().subList(0, meta.getLore().size() - 3);
		meta.setLore(newLore);
		for (Enchantment ench : meta.getEnchants().keySet())
			meta.removeEnchant(ench);
		book.setItemMeta(meta);
		inv.setItem(28, book);

		inv.setItem(20, page > 1 ? ConfigItems.PREVIOUS_PAGE.getItem() : ConfigItems.FILL.getItem());
		inv.setItem(24, max < ingredients.size() ? ConfigItems.NEXT_PAGE.getItem() : ConfigItems.FILL.getItem());

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (!MMOUtils.isMetaItem(event.getCurrentItem(), false))
			return;

		NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
		switch (nbtItem.getString("ItemId")) {
			case "CONFIRM":
				previous.processRecipe(recipe);
				previous.open();
				break;
			case "PREVIOUS_PAGE":
				page--;
				open();
				break;
			case "NEXT_PAGE":
				page++;
				open();
				break;
			case "BACK":
				previous.open();
				break;
		}
	}
}
