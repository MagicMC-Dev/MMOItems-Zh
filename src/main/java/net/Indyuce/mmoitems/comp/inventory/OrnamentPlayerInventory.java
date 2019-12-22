package net.Indyuce.mmoitems.comp.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class OrnamentPlayerInventory implements PlayerInventory, Listener {
	public OrnamentPlayerInventory() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		list.add(new EquippedItem(player.getEquipment().getItemInMainHand(), EquipmentSlot.MAIN_HAND));
		list.add(new EquippedItem(player.getEquipment().getItemInOffHand(), EquipmentSlot.OFF_HAND));
		for (ItemStack armor : player.getInventory().getArmorContents())
			list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		for (ItemStack item : player.getInventory().getContents()) {
			NBTItem nbtItem;
			if (item != null && (nbtItem = MMOLib.plugin.getNMS().getNBTItem(item)).hasType() && nbtItem.getType().getEquipmentType() == EquipmentSlot.ANY)
				list.add(new EquippedItem(nbtItem, EquipmentSlot.ANY));
		}

		return list;
	}

	@EventHandler(ignoreCancelled = true)
	public void a(EntityPickupItemEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			NBTItem nbt = NBTItem.get(event.getItem().getItemStack());
			if (nbt.hasType() && nbt.getType().getEquipmentType() == EquipmentSlot.ANY)
				PlayerData.get((Player) event.getEntity()).updateInventory();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void b(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if (nbt.hasType() && nbt.getType().getEquipmentType() == EquipmentSlot.ANY)
			PlayerData.get(event.getPlayer()).updateInventory();
	}
}
