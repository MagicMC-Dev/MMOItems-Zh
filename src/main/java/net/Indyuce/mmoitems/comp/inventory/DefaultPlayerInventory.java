package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells MMOItems where to find equipment.
 * <p></p>
 * Armor slots, mainhand and offhand.
 */
public class DefaultPlayerInventory implements PlayerInventory {
	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		// Mainhand
		list.add(new EquippedItem(player.getEquipment().getItemInMainHand(), EquipmentSlot.MAIN_HAND));

		// Offhand
		list.add(new EquippedItem(player.getEquipment().getItemInOffHand(), EquipmentSlot.OFF_HAND));

		// Armour
		for (ItemStack armor : player.getInventory().getArmorContents())
			list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		return list;
	}
}
