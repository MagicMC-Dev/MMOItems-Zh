package net.Indyuce.mmoitems.comp.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import ru.endlesscode.rpginventory.api.InventoryAPI;

public class RPGInventoryHook implements PlayerInventory, Listener {
	private final boolean ornaments;

	/*
	 * RPGInventory is outdated. MI still supports it but it shall NEVER be
	 * considered a priority to keep MI compatible OR performance efficient with
	 * RPGInventory
	 */
	public RPGInventoryHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
		if (ornaments = MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory"))
			Bukkit.getPluginManager().registerEvents(new OrnamentPlayerInventory(), MMOItems.plugin);
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		list.add(new EquippedItem(player.getInventory().getItemInMainHand(), EquipmentSlot.MAIN_HAND));
		list.add(new EquippedItem(player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND));

		for (ItemStack passive : InventoryAPI.getPassiveItems(player))
			if (passive != null)
				list.add(new EquippedItem(passive, EquipmentSlot.ANY));
		for (ItemStack armor : player.getInventory().getArmorContents())
			if (armor != null)
				list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		if (ornaments)
			for (ItemStack item : player.getInventory().getContents()) {
				NBTItem nbtItem;
				if (item != null && (nbtItem = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item)).hasType() && Type.get(nbtItem.getType()).getEquipmentType() == EquipmentSlot.ANY)
					list.add(new EquippedItem(nbtItem, EquipmentSlot.ANY));
			}

		return list;
	}

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (InventoryAPI.isRPGInventory(event.getInventory()))
			PlayerData.get((Player) event.getPlayer()).updateInventory();
	}
}
