package net.Indyuce.mmoitems.listener;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;

public class DurabilityListener implements Listener {
	private final List<DamageCause> ignoredCauses = Arrays.asList(DamageCause.DROWNING, DamageCause.SUICIDE, DamageCause.FALL, DamageCause.VOID,
			DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.POISON, DamageCause.WITHER, DamageCause.STARVATION, DamageCause.MAGIC);

	/**
	 * Handles custom durability for player heads
	 */
	@EventHandler(ignoreCancelled = true)
	public void playerDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER || ignoredCauses.contains(event.getCause()))
			return;

		Player player = (Player) event.getEntity();
		if (player.getEquipment().getHelmet() == null || player.getEquipment().getHelmet().getType() != Material.PLAYER_HEAD)
			return;

		ItemStack helmet = player.getEquipment().getHelmet();
		DurabilityItem item = new DurabilityItem(player, helmet);

		if (item.isValid()) {
			/*
			 * Calculate item durability loss
			 *
			 * This uses the vanilla formula of 1 durability per 4 damage.
			 * (rounded down, but never below 1)
			 */
			item.decreaseDurability(Math.max((int) event.getDamage() / 4, 1));

			/*
			 * If the item is broken and if it is meant to be lost when broken,
			 * do NOT cancel the event and make sure the item is destroyed
			 */
			if (item.isBroken() && item.isLostWhenBroken()) {
				player.getEquipment().setHelmet(null);
				return;
			}

			helmet.setItemMeta(item.toItem().getItemMeta());
			player.getEquipment().setHelmet(helmet);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void itemDamage(PlayerItemDamageEvent event) {
		DurabilityItem item = new DurabilityItem(event.getPlayer(), event.getItem());

		if (item.isValid()) {
			/*
			 * Calculate item durability loss
			 */
			item.decreaseDurability(event.getDamage());

			/*
			 * If the item is broken and if it is meant to be lost when broken,
			 * do NOT cancel the event and make sure the item is destroyed
			 */
			if (item.isBroken() && item.isLostWhenBroken()) {
				event.setDamage(999);
				return;
			}

			event.setCancelled(true);
			event.getItem().setItemMeta(item.toItem().getItemMeta());
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void mendEvent(PlayerItemMendEvent event) {
		DurabilityItem durItem = new DurabilityItem(event.getPlayer(), event.getItem());
		if (durItem.isValid())
			event.getItem().setItemMeta(durItem.addDurability(event.getRepairAmount()).toItem().getItemMeta());
	}
}
