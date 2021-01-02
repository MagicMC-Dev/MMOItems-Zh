package net.Indyuce.mmoitems.listener;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;

public class DurabilityListener implements Listener {
	private final List<DamageCause> ignoredCauses = Arrays.asList(DamageCause.DROWNING, DamageCause.SUICIDE, DamageCause.FALL, DamageCause.VOID,
			DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.POISON, DamageCause.WITHER, DamageCause.STARVATION, DamageCause.MAGIC);
	private final EquipmentSlot[] slots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

	/**
	 * Handles custom durability for non-'vanilla durability' items
	 */
	@EventHandler(ignoreCancelled = true)
	public void playerDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER || ignoredCauses.contains(event.getCause()))
			return;

		Player player = (Player) event.getEntity();
		int damage = Math.max((int) event.getDamage() / 4, 1);
		for(EquipmentSlot slot : slots)
			if(hasItem(player, slot))
				handleVanillaDamage(player.getInventory().getItem(slot), player, slot, damage);
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
		if (durItem.isValid()) {
			event.getItem().setItemMeta(durItem.addDurability(event.getRepairAmount()).toItem().getItemMeta());
			event.setCancelled(true);
		}
	}

	private void handleVanillaDamage(ItemStack stack, Player player, EquipmentSlot slot, int damage) {
		DurabilityItem item = new DurabilityItem(player, stack);

		if (item.isValid()) {
			item.decreaseDurability(damage);

			if (item.isBroken() && item.isLostWhenBroken()) {
				player.getInventory().setItem(slot, null);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
				return;
			}

			player.getInventory().getItem(slot).setItemMeta(item.toItem().getItemMeta());
		}
	}

	private boolean hasItem(Player player, EquipmentSlot slot) {
		return player.getInventory().getItem(slot) != null && player.getInventory().getItem(slot).getType() != Material.AIR;
	}
}
