package net.Indyuce.mmoitems.api.interaction.util;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import org.bukkit.entity.Player;

import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;

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

		// Cannot update null player
		if (getPlayer() == null) { return; }

		// If it broke (funny)
		if (isBroken()) {

			// Attempt to counter by downgrading
			if (isDowngradedWhenBroken()) {

				ItemStack counterUpgraded = shouldBreakWhenDowngraded();
				if (counterUpgraded != null) {

					// Edit item
					getNBTItem().getItem().setItemMeta(counterUpgraded.getItemMeta());

					// No more
					return;
				}
			}

			// Still here? Remove if lost when broken
			if (isLostWhenBroken()) {

				// Delete item
				if (slot == EquipmentSlot.OFF_HAND) {
					getPlayer().getInventory().setItemInOffHand(null);

				} else { getPlayer().getInventory().setItemInMainHand(null); }

				// No more
				return;
			}
		}

		getNBTItem().getItem().setItemMeta(toItem().getItemMeta());
	}
}
