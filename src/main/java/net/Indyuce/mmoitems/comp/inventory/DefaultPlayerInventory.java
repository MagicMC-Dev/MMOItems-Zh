package net.Indyuce.mmoitems.comp.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;

public class DefaultPlayerInventory implements PlayerInventory {
	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		list.add(new EquippedItem(player.getEquipment().getItemInMainHand(), EquipmentSlot.MAIN_HAND));
		list.add(new EquippedItem(player.getEquipment().getItemInOffHand(), EquipmentSlot.OFF_HAND));
		for (ItemStack armor : player.getInventory().getArmorContents())
			list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		if(MMOItems.plugin.getLanguage().iterateWholeInventory)
			for (ItemStack item : player.getInventory().getContents()) {
				if(item != null)
					list.add(new EquippedItem(item, EquipmentSlot.ANY));
			}
		
		return list;
	}
}
