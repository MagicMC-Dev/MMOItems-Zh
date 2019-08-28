package net.Indyuce.mmoitems.api.interaction.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractItem {
	private final EquipmentSlot slot;
	private final ItemStack item;

	/*
	 * determines in which hand the player has a specific item, prioritizing the
	 * main hand. it is used to easily replace one of the player's hand items
	 * when he uses tools like flint & steel, shears, bows, etc.
	 */
	public InteractItem(Player player, Material material) {
		this.slot = hasItem(player.getInventory().getItemInMainHand(), material) ? EquipmentSlot.HAND : hasItem(player.getInventory().getItemInOffHand(), material) ? EquipmentSlot.OFF_HAND : null;
		this.item = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
	}

	/*
	 * works the same but with a material suffit like _HOE which allows to use
	 * that class for any tool made with any ingot/material
	 */
	public InteractItem(Player player, String suffix) {
		this.slot = hasItem(player.getInventory().getItemInMainHand(), suffix) ? EquipmentSlot.HAND : hasItem(player.getInventory().getItemInOffHand(), suffix) ? EquipmentSlot.OFF_HAND : null;
		this.item = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
	}

	public InteractItem(Player player, EquipmentSlot slot) {
		this.slot = slot;
		this.item = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
	}

	public boolean hasItem() {
		return slot != null;
	}

	public ItemStack getItem() {
		return item;
	}

	private boolean hasItem(ItemStack item, Material material) {
		return item != null && item.getType() == material;
	}

	private boolean hasItem(ItemStack item, String suffix) {
		return item != null && item.getType().name().endsWith(suffix);
	}

	public void setItem(ItemStack item) {
		if (item != null && hasItem())
			this.item.setItemMeta(item.getItemMeta());
	}
}
