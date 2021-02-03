package net.Indyuce.mmoitems.comp.itemglow;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.api.item.NBTItem;

public class NoGlowListener implements Listener {
	
	/*
	 * only applies item hints.
	 */
	@EventHandler
	public void a(ItemSpawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();
		String id = NBTItem.get(item).getString("MMOITEMS_TIER");
		if (MMOItems.plugin.getTiers().has(id) && MMOItems.plugin.getTiers().get(id).isHintEnabled()) {
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(item.getItemMeta().getDisplayName());
		}
	}
}
