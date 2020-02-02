package net.Indyuce.mmoitems.gui.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.CraftingEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.gui.edition.RecipeEdition;

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

			if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
				return;

			if (!item.getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + ""))
				return;

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub))
				for (ItemStack drop : player.getInventory().addItem(event.getInventory().getItem(4)).values())
					player.getWorld().dropItemNaturally(player.getLocation(), drop);

			Type type = ((EditionInventory) inventory).getItemType();
			String id = ((EditionInventory) inventory).getItemId();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back")) {
				if(inventory instanceof ItemEdition) new ItemBrowser(player, type).open();
				else if(inventory instanceof RecipeEdition) new CraftingEdition(player, type, id).open(((EditionInventory) inventory).getPreviousPage());
				else new ItemEdition(player, type, id).onPage(((EditionInventory) inventory).getPreviousPage()).open();
			}
		}
	}
}
