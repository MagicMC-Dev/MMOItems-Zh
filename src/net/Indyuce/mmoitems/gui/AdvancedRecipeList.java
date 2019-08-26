package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.AdvancedRecipe;
import net.Indyuce.mmoitems.api.Message;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class AdvancedRecipeList extends PluginInventory {
	private Type type;
	
	public AdvancedRecipeList(Player player, Type type) {
		super(player);
		this.type = type;
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 27, Message.ADVANCED_RECIPES.formatRaw(ChatColor.UNDERLINE));

		Integer[] slots = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
		List<AdvancedRecipe> recipes = MMOItems.plugin.getRecipes().getTypeRecipes(type);
		int min = (page - 1) * 21;
		int max = page * 21;
		for (int j = min; j < max && j < recipes.size(); j++) {

			AdvancedRecipe recipe = recipes.get(j);
			ItemStack item = recipe.getPreviewItem().clone();

			ItemMeta itemMeta = item.getItemMeta();
			List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<String>();
			itemLore.add("");
			itemLore.add(Message.CLICK_ADVANCED_RECIPE.formatRaw(ChatColor.YELLOW, "#d", AltChar.listDash));
			itemMeta.setLore(itemLore);
			item.setItemMeta(itemMeta);

			inv.setItem(slots[j - min], NBTItem.get(item).addTag(new ItemTag("itemId", recipe.getItemId())).toItem());
		}

		for (int j : slots)
			if (inv.getItem(j) == null)
				inv.setItem(j, ConfigItem.NO_ITEM.getItem());

		inv.setItem(9, page > 1 ? ConfigItem.PREVIOUS_PAGE.getItem() : ConfigItem.BACK.getItem());
		inv.setItem(17, inv.getItem(slots[slots.length - 1]).equals(ConfigItem.NO_ITEM.getItem()) ? null : ConfigItem.NEXT_PAGE.getItem());

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getClickedInventory() != event.getInventory())
			return;

		if (MMOUtils.isPluginItem(item, false)) {
			if (item.equals(ConfigItem.BACK.getItem()))
				new AdvancedRecipeTypeList(player).open();

			if (item.equals(ConfigItem.NEXT_PAGE.getItem())) {
				page++;
				open();
			}

			if (item.equals(ConfigItem.PREVIOUS_PAGE.getItem()) && page > 1) {
				page--;
				open();
			}
		}

		// show recipe when click
		String tag = NBTItem.get(item).getString("itemId");
		if (!tag.equals(""))
			new AdvancedRecipePreview(player, type, tag).open();
	}
}
