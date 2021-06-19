package net.Indyuce.mmoitems.api.interaction.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractItem {
	private final EquipmentSlot slot;
	private final ItemStack item;

	/**
	 * Used to determine in which hand a player has a specific item prioritizing
	 * the main hand just like vanilla MC. For example, this is used when
	 * throwing tridents to register weapon effects on the trident
	 * 
	 * @param player
	 *            Player doing the action
	 * @param material
	 *            Item to look for
	 */
	public InteractItem(Player player, Material material) {
		this.slot = hasItem(player.getInventory().getItemInMainHand(), material) ? EquipmentSlot.HAND
				: hasItem(player.getInventory().getItemInOffHand(), material) ? EquipmentSlot.OFF_HAND : null;
		this.item = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
	}

	/**
	 * Used to determine in which hand a player has a specific item prioritizing
	 * the main hand just like vanilla MC. For example, this is used when
	 * throwing tridents to register weapon effects on the trident
	 * 
	 * @param player
	 *            Player doing the action
	 * @param suffix
	 *            Material suffix to look for eg "_HOE" looks for a hoe in the
	 *            player hands
	 */
	@Deprecated
	public InteractItem(Player player, String suffix) {
		this.slot = hasItem(player.getInventory().getItemInMainHand(), suffix) ? EquipmentSlot.HAND
				: hasItem(player.getInventory().getItemInOffHand(), suffix) ? EquipmentSlot.OFF_HAND : null;
		this.item = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
	}

	public EquipmentSlot getSlot() {
		return slot;
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
}
