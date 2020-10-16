package net.Indyuce.mmoitems.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;

public class NoClipItem implements Listener {
	private final Item item;

	/**
	 * Util class which creates an item which cannot be picked up. Item is
	 * removed if it tries to go through a nether portal
	 * 
	 * @param loc
	 *            Spawn location of the item
	 * @param item
	 *            ItemStack used to summon the entity
	 */
	public NoClipItem(Location loc, ItemStack item) {
		item.setAmount(1);

		this.item = loc.getWorld().dropItem(loc, item);
		this.item.setPickupDelay(1000000);

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	public Item getEntity() {
		return item;
	}

	public void close() {
		item.remove();

		EntityPortalEnterEvent.getHandlerList().unregister(this);
		InventoryPickupItemEvent.getHandlerList().unregister(this);
	}

	// Stops items from being picked up by hoppers and portals and then duping them.
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void a(InventoryPickupItemEvent event) {
		if (event.getItem().equals(item)) {
			event.setCancelled(true);
			close();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void b(EntityPortalEnterEvent event) {
		if (event.getEntity().equals(item))
			close();
	}
}
