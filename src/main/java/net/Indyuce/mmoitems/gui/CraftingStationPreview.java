package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient.IngredientInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.md_5.bungee.api.ChatColor;

public class CraftingStationPreview extends PluginInventory {
	private final int previousPage;
	private final CraftingStation station;
	private final RecipeInfo recipe;

	private List<IngredientInfo> ingredients = new ArrayList<>();
	
	private static final int[]
			slots = { 12, 13, 14, 21, 22, 23, 30, 31, 32 },
			fill = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 15, 17, 18, 19, 25, 26, 27, 29, 33, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 };

	public CraftingStationPreview(Player player, CraftingStation station, RecipeInfo recipe, int previousPage) {
		super(player);

		this.previousPage = previousPage;
		this.station = station;
		this.recipe = recipe;
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 45, "Recipe Preview");
		ingredients.clear();
		ingredients.addAll(recipe.getIngredients());
		
		int min = (page - 1) * slots.length,
			max = page * slots.length;
		for (int j = min; j < max; j++) {
			if (j >= ingredients.size()) {
				if (station.getItemOptions().hasNoRecipe())
					inv.setItem(slots[j - min], null);
				continue;
			}

			inv.setItem(slots[j - min], ingredients.get(j).getIngredient().generateItemStack());
		}

		for (int slot : fill)
			inv.setItem(slot, ConfigItem.FILL.getItem());

		if(recipe.getRecipe() instanceof CraftingRecipe)
			inv.setItem(16, ((CraftingRecipe) recipe.getRecipe()).getOutput().getPreview());
		if(recipe.getRecipe() instanceof UpgradingRecipe) {
			ItemStack stack = ((UpgradingRecipe) recipe.getRecipe()).getItem().getPreview();
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(meta.getDisplayName() + ChatColor.translateAlternateColorCodes('&', " &a+1!"));
			stack.setItemMeta(meta);
			inv.setItem(16, stack);
		}
		
		inv.setItem(10, ConfigItem.BACK.getItem());
		inv.setItem(34, ConfigItem.CONFIRM.getItem());
		ItemStack book = recipe.display();
		book.setType(Material.KNOWLEDGE_BOOK);
		ItemMeta meta = book.getItemMeta();
		List<String> newLore = meta.getLore().subList(0, meta.getLore().size() - 3);
		meta.setLore(newLore);
		for(Enchantment ench : meta.getEnchants().keySet())
			meta.removeEnchant(ench);
		book.setItemMeta(meta);
		inv.setItem(28, book);


		inv.setItem(20, page > 1 ? ConfigItem.PREVIOUS_PAGE.getItem() : ConfigItem.FILL.getItem());
		inv.setItem(24, max < ingredients.size() ? ConfigItem.NEXT_PAGE.getItem() : ConfigItem.FILL.getItem());
		
		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);

		if (!MMOUtils.isPluginItem(event.getCurrentItem(), false))
			return;

		if (event.getCurrentItem().isSimilar(ConfigItem.CONFIRM.getItem())) {
			CraftingStationView csv = new CraftingStationView(player, station, previousPage);
			csv.processRecipe(recipe);
			csv.open();
			return;
		}
		
		if (event.getCurrentItem().isSimilar(ConfigItem.PREVIOUS_PAGE.getItem())) {
			page--;
			open();
			return;
		}
		
		if (event.getCurrentItem().isSimilar(ConfigItem.NEXT_PAGE.getItem())) {
			page++;
			open();
			return;
		}

		if (event.getCurrentItem().isSimilar(ConfigItem.BACK.getItem())) {
			new CraftingStationView(player, station, previousPage).open();
			return;
		}
	}
}
