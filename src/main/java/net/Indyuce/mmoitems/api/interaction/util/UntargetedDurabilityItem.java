package net.Indyuce.mmoitems.api.interaction.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import net.mmogroup.mmolib.api.item.NBTItem;

public class UntargetedDurabilityItem extends DurabilityItem {
	private final EquipmentSlot slot;

	/*
	 * Allows to handle custom durability for target weapons when they are
	 * left/right click while using the same durability system for both weapon
	 * types
	 */
	public UntargetedDurabilityItem(Player player, NBTItem item, EquipmentSlot slot) {
		super(player, item);

		this.slot = slot;
	}

	@Override
	public UntargetedDurabilityItem decreaseDurability(int loss) {
		return (UntargetedDurabilityItem) super.decreaseDurability(loss);
	}

	public void update() {

		if (isBroken() && isLostWhenBroken()) {
			if (slot == EquipmentSlot.OFF_HAND)
				getPlayer().getInventory().setItemInOffHand(null);
			else
				getPlayer().getInventory().setItemInMainHand(null);
			return;
		}

		getNBTItem().getItem().setItemMeta(toItem().getItemMeta());
	}
}
