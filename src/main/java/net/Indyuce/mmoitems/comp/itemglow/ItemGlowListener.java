package net.Indyuce.mmoitems.comp.itemglow;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.glow.GlowAPI;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import io.lumine.mythic.lib.api.item.NBTItem;

public class ItemGlowListener implements Listener {

	/*
	 * applies both item hints & item glow depending on the tier of the item
	 * dropped.
	 */
	@EventHandler
	public void a(ItemSpawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();
		String id = NBTItem.get(item).getString("MMOITEMS_TIER");
		if (MMOItems.plugin.getTiers().has(id)) {
			ItemTier tier = MMOItems.plugin.getTiers().get(id);
			if (tier.isHintEnabled()) {
				event.getEntity().setCustomNameVisible(true);
				event.getEntity().setCustomName(item.getItemMeta().getDisplayName());
			}

			if (tier.hasColor())
				GlowAPI.setGlowing(event.getEntity(), tier.getColor().toGlow().get(), event.getEntity().getWorld().getPlayers());
		}
	}

	@EventHandler
	public void b(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		for (Item entity : player.getWorld().getEntitiesByClass(Item.class)) {
			ItemStack item = entity.getItemStack();
			String id = NBTItem.get(item).getString("MMOITEMS_TIER");
			if (MMOItems.plugin.getTiers().has(id)) {
				ItemTier tier = MMOItems.plugin.getTiers().get(id);
				if (tier.hasColor())
					Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> GlowAPI.setGlowing(entity, tier.getColor().toGlow().get(), player));
			}
		}
	}

	@EventHandler
	public void c(PlayerTeleportEvent event) {
		if (event.getFrom().getWorld().equals(event.getTo().getWorld()))
			return;

		Player player = event.getPlayer();
		for (Item entity : player.getWorld().getEntitiesByClass(Item.class)) {
			ItemStack item = entity.getItemStack();
			String id = NBTItem.get(item).getString("MMOITEMS_TIER");
			if (MMOItems.plugin.getTiers().has(id)) {
				ItemTier tier = MMOItems.plugin.getTiers().get(id);
				if (tier.hasColor())
					Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> GlowAPI.setGlowing(entity, tier.getColor().toGlow().get(), player));
			}
		}
	}
}
