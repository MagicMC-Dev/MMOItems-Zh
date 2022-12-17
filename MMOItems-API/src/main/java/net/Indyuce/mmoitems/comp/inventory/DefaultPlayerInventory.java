package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells MMOItems where to find equipment.
 * <p></p>
 * Armor slots, mainhand and offhand.
 */
public class DefaultPlayerInventory implements PlayerInventory {

	@Override
	@NotNull
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		if (player.getEquipment() == null) { return list; }

		// Mainhand
		list.add(new SlotEquippedItem(player, -7, player.getEquipment().getItemInMainHand(), EquipmentSlot.MAIN_HAND));

		// Offhand
		list.add(new SlotEquippedItem(player, -106, player.getEquipment().getItemInOffHand(), EquipmentSlot.OFF_HAND));

		// Armor
		list.add(new SlotEquippedItem(player, 103, player.getEquipment().getHelmet(), EquipmentSlot.ARMOR));
		list.add(new SlotEquippedItem(player, 102, player.getEquipment().getChestplate(), EquipmentSlot.ARMOR));
		list.add(new SlotEquippedItem(player, 101, player.getEquipment().getLeggings(), EquipmentSlot.ARMOR));
		list.add(new SlotEquippedItem(player, 100, player.getEquipment().getBoots(), EquipmentSlot.ARMOR));

		return list;
	}
}
