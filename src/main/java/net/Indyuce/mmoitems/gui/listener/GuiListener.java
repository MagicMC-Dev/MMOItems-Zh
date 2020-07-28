package net.Indyuce.mmoitems.gui.listener;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.CraftingEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.gui.edition.RecipeEdition;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {
	@EventHandler
	public void a(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();

		if (event.getInventory().getHolder() instanceof PluginInventory) {
			PluginInventory inventory = (PluginInventory) event.getInventory().getHolder();
			inventory.whenClicked(event);

			// if inventory is edition inventory
			// then the player can click specific items
			if (!(inventory instanceof EditionInventory))
				return;

			if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
				return;

			if (!item.getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + ""))
				return;

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub)) {

				// simply give the item if left click
				if (event.getAction() == InventoryAction.PICKUP_ALL){
					for (ItemStack drop : player.getInventory().addItem(event.getInventory().getItem(4)).values())
						player.getWorld().dropItemNaturally(player.getLocation(), drop);
					if (NBTItem.get(event.getInventory().getItem(4)).getBoolean("UNSTACKABLE")) { // this refreshes the item if it's unstackable
						((EditionInventory) inventory).updateCachedItem();
						event.getInventory().setItem(4, ((EditionInventory) inventory).getCachedItem());
					}
				}


				// reroll stats if right click
				else if (event.getAction() == InventoryAction.PICKUP_HALF) {
					for (ItemStack drop : player.getInventory().addItem(event.getInventory().getItem(4)).values())
						player.getWorld().dropItemNaturally(player.getLocation(), drop);

					((EditionInventory) inventory).updateCachedItem();
					event.getInventory().setItem(4, ((EditionInventory) inventory).getCachedItem());
				}
			}

			MMOItem mmoitem = ((EditionInventory) inventory).getEdited();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back")) {
				if (inventory instanceof ItemEdition)
					new ItemBrowser(player, mmoitem.getType()).open();
				else if (inventory instanceof RecipeEdition)
					new CraftingEdition(player, mmoitem).open(((EditionInventory) inventory).getPreviousPage());
				else
					new ItemEdition(player, mmoitem).onPage(((EditionInventory) inventory).getPreviousPage()).open();
			}
		}
	}
}
