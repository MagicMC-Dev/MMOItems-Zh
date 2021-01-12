package net.Indyuce.mmoitems.api.player.inventory;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EquippedItem {
	private final NBTItem item;
	private final EquipmentSlot slot;

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 * @param slot The corresponding MMOItems slot type
	 */
	public EquippedItem(ItemStack item, EquipmentSlot slot) {
		this(MMOLib.plugin.getVersion().getWrapper().getNBTItem(item), slot);
	}

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 * @param slot The corresponding MMOItems slot type, must not be null!
	 */
	public EquippedItem(NBTItem item, EquipmentSlot slot) {
		this.item = item;
		this.slot = slot;
	}

	public NBTItem getItem() {
		return item;
	}
	
	public EquipmentSlot getSlot() {
		return slot;
	}

	/**
	 * The slot this equipped item is defined to be, will this <code>Type</code>
	 * actually add its stats to the player when held here?
	 * <p></p>
	 * An <code>OFF_CATALYST</code> may only add in the <code>OFFHAND</code>, and such.
	 */
	public boolean matches(@NotNull Type type) { return matches(type, null); }

	/**
	 * The slot this equipped item is defined to be, will this <code>Type</code>
	 * actually add its stats to the player when held here?
	 * <p></p>
	 * An <code>OFF_CATALYST</code> may only add in the <code>OFFHAND</code>, and such.
	 * <p></p>
	 * There is one type that depends on what is held in another slot: <code>EITHER_HAND</code>.
	 * If there is no information, it will behave like <code>MAIN_HAND</code>, but if there is,
	 * if will only <i>match</i> if the mainhand is not <code>MAIN_HAND</code> nor <code>EITHER_HAND</code>
	 */
	public boolean matches(@NotNull Type type, @Nullable EquipmentSlot mainheld) {

		// Get Type Equipment
		EquipmentSlot slotT = type.getEquipmentType();

		// Matches any?
		if (slot == EquipmentSlot.ANY) { return true; }


		// Is it Either Mainhand or Offhand?
		if (slotT == EquipmentSlot.EITHER_HAND) {

			// Is it held in mainhand?
			if (slot == EquipmentSlot.MAIN_HAND) { return true; }

			// What is in the main hand?
			if (mainheld != null) {

				// Something else held? nope
				if (mainheld == EquipmentSlot.MAIN_HAND) { return false; }
				if (mainheld == EquipmentSlot.EITHER_HAND) { return false; }

				// WIll match if the slot is a hand
				return slot.isHand();

			// No information was provided, proceed as MAIN_HAND
			} else {

				// WIll behave as MAIN_HAND
				slotT = EquipmentSlot.OFF_HAND;
			}
		}

		// Is it held regardless of hand used?
		if (slotT == EquipmentSlot.BOTH_HANDS && slot.isHand()) { return true; }
		if (slot == EquipmentSlot.BOTH_HANDS && slotT.isHand()) { return true; }

		// Is it the same type?
		return slot == slotT;
	}
}