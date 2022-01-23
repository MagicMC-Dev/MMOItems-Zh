package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TODO
 * <p>
 * It's one of the most urgent systems to update. Moving everything to a new
 * class to mark everything that needs to be changed
 *
 * @author indyuce
 */
public class InventoryUpdateHandler {
	private final PlayerData player;

	private final List<EquippedPlayerItem> items = new ArrayList<>();

	@Deprecated
	public ItemStack helmet = null, chestplate = null, leggings = null, boots = null, hand = null, offhand = null;

	/**
	 * Used to handle player inventory updates.
	 */
	public InventoryUpdateHandler(PlayerData player) { this.player = player; }

	/**
	 * @return All equipped MMOItems in the player's inventory. Also includes
	 *         items from custom inventory plugins like MMOInventory
	 */
	public List<EquippedPlayerItem> getEquipped() {
		return items;
	}

	public void updateCheck() {
		if (!player.isOnline()) { return; }

		PlayerInventory inv = player.getPlayer().getInventory();
		if (isNotSame(helmet, inv.getHelmet()) || isNotSame(chestplate, inv.getChestplate()) || isNotSame(leggings, inv.getLeggings())
				|| isNotSame(boots, inv.getBoots()) || isNotSame(hand, inv.getItemInMainHand()) || isNotSame(offhand, inv.getItemInOffHand()))
			player.updateInventory();
	}

	/**
	 * Schedules an inventory update in one tick
	 */
	public void scheduleUpdate() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, player::updateInventory);
	}

	private boolean isNotSame(ItemStack item, ItemStack item1) {
		return !Objects.equals(item, item1);
	}
}
