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
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
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

		for (ItemStack item : InventoryAPI.getPassiveItems(player))
			list.add(new EquippedItem(item, EquipmentSlot.ACCESSORY));
		for (ItemStack item : InventoryAPI.getActiveItems(player))
			list.add(new EquippedItem(item, EquipmentSlot.BOTH_HANDS));
		for (ItemStack armor : player.getInventory().getArmorContents())
			list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		if (ornaments)
			for (ItemStack item : player.getInventory().getContents()) {
				NBTItem nbtItem;
				if (item != null && (nbtItem = MMOItems.plugin.getNMS().getNBTItem(item)).hasType() && nbtItem.getType().getEquipmentType() == EquipmentSlot.ANY)
					list.add(new EquippedItem(nbtItem, EquipmentSlot.ANY));
			}

		return list;
	}

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (InventoryAPI.isRPGInventory(event.getInventory()))
			PlayerData.get((Player) event.getPlayer()).checkForInventoryUpdate();
	}
}
