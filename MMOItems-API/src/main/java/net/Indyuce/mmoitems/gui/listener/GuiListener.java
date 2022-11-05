package net.Indyuce.mmoitems.gui.listener;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeListGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
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
			if (!(inventory instanceof EditionInventory) || event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false)
					|| !item.getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + ""))
				return;

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub)) {

				// simply give the item if left click
				if (event.getAction() == InventoryAction.PICKUP_ALL) {
					for (ItemStack drop : player.getInventory().addItem(event.getInventory().getItem(4)).values())
						player.getWorld().dropItemNaturally(player.getLocation(), drop);

					// this refreshes the item if it's unstackable
					if (NBTItem.get(event.getInventory().getItem(4)).getBoolean("MMOITEMS_UNSTACKABLE")) {
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

			MMOItemTemplate template = ((EditionInventory) inventory).getEdited();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back")) {

				// Open the Item Browser yes
				if (inventory instanceof ItemEdition) { new ItemBrowser(player, template.getType()).open(); }

				// Open the RECIPE TYPE BROWSER stat thing
				else if ((inventory instanceof RecipeListGUI)) { new RecipeBrowserGUI(player, template).open(((EditionInventory) inventory).getPreviousPage()); }

				// Open the RECIPE LIST thing
				else if ((inventory instanceof RecipeMakerGUI)) { new RecipeListGUI(player, template, ((RecipeMakerGUI) inventory).getRecipeRegistry()).open(((EditionInventory) inventory).getPreviousPage()); }

				// Just open the ITEM EDITION I guess
				else { new ItemEdition(player, template).onPage(((EditionInventory) inventory).getPreviousPage()).open(); }
			}
		}
	}
}
