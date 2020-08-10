package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.CraftingType;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.util.AltChar;

public class CraftingEdition extends EditionInventory {
	private static final int[] slots = { 21, 22, 23, 30, 31, 32 };

	public CraftingEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, MMOLib.plugin.getVersion().isStrictlyHigher(1, 14) ? 45 : 36,
				ChatColor.UNDERLINE + "Crafting Recipes: " + template.getId());

		int n = 0;

		for (CraftingType ctype : CraftingType.values()) {
			if (!ctype.shouldAdd())
				continue;
			ItemStack craftingEvent = ctype.getItem();
			ItemMeta craftingEventItem = craftingEvent.getItemMeta();
			craftingEventItem.addItemFlags(ItemFlag.values());
			craftingEventItem.setDisplayName(ChatColor.GREEN + ctype.getName());
			List<String> eventLore = new ArrayList<String>();
			eventLore.add(ChatColor.GRAY + ctype.getLore());
			if (!mmoitem.getType().getConfigFile().getConfig().contains(mmoitem.getId() + ".crafting." + ctype.name().toLowerCase())) {
				eventLore.add("");
				eventLore.add(ChatColor.RED + "No recipes found.");
			}
			eventLore.add("");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this recipe.");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove recipe.");
			craftingEventItem.setLore(eventLore);
			craftingEvent.setItemMeta(craftingEventItem);

			inv.setItem(slots[n], craftingEvent);
			n += 1;
		}

		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		CraftingType corresponding = CraftingType.getBySlot(event.getSlot());
		if (corresponding == null)
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			if (event.getSlot() == 21 || event.getSlot() == 22)
				new RecipeEdition(player, mmoitem, event.getSlot() == 22).open(getPreviousPage());
			else
				new StatEdition(this, ItemStat.CRAFTING, "item", corresponding.name().toLowerCase()).enable(
						"Write in the chat the item, tickspeed and exp you want.", "Format: '[ITEM] [TICKS] [EXP]'",
						"[ITEM] = '[MATERIAL]' or '[MATERIAL]:[DURABILITY]' or '[TYPE].[ID]'");
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = mmoitem.getType().getConfigFile();
			if (!config.getConfig().contains(mmoitem.getId() + ".crafting." + corresponding.name().toLowerCase()))
				return;

			config.getConfig().set(mmoitem.getId() + ".crafting." + corresponding.name().toLowerCase(), null);
			player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + corresponding.getName() + " recipe.");

			if (config.getConfig().getConfigurationSection(mmoitem.getId() + ".crafting") == null)
				config.getConfig().set(mmoitem.getId() + ".crafting", null);

			registerTemplateEdition(config, true);
		}
	}
}