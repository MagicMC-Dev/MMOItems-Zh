package net.Indyuce.mmoitems.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.AdvancedWorkbench;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class AdvancedWorkbenchListener implements Listener {
	@EventHandler
	public void a(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != VersionMaterial.CRAFTING_TABLE.toMaterial())
			return;

		boolean shiftClick = MMOItems.plugin.getConfig().getBoolean("advanced-workbench.open-when.shift-click");
		boolean simpleClick = MMOItems.plugin.getConfig().getBoolean("advanced-workbench.open-when.simple-click");

		Player player = event.getPlayer();
		if ((shiftClick && player.isSneaking()) || (simpleClick && !player.isSneaking())) {
			event.setCancelled(true);
			new AdvancedWorkbench(player).open();
		}
	}

	@EventHandler
	public void b(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		Inventory inv = event.getInventory();
		if (inv.getHolder() instanceof AdvancedWorkbench)
			for (int j : MMOItems.plugin.getRecipes().recipeSlots) {
				ItemStack drop = inv.getItem(j);
				if (drop != null)
					player.getWorld().dropItemNaturally(player.getLocation(), drop);
			}
	}
}