package net.Indyuce.mmoitems.comp.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import ru.endlesscode.rpginventory.api.InventoryAPI;

public class RPGInventoryHook implements PlayerInventory, Listener {
	public RPGInventoryHook(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
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

		return list;
	}

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (InventoryAPI.isRPGInventory(event.getInventory()))
			PlayerData.get((Player) event.getPlayer()).checkForInventoryUpdate();
	}
}
