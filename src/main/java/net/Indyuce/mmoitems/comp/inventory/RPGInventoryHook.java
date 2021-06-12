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
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import ru.endlesscode.rpginventory.api.InventoryAPI;

/**
 * Tells MMOItems where to find additional equipment.
 * <p></p>
 * RPGInventory stuff - Passive Items
 */
public class RPGInventoryHook implements PlayerInventory, Listener {

	/*
	 * RPGInventory is outdated. MI still supports it but it shall NEVER be
	 * considered a priority to keep MI compatible OR performance efficient with
	 * RPGInventory
	 */
	public RPGInventoryHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		for (ItemStack passive : InventoryAPI.getPassiveItems(player))
			if (passive != null)
				list.add(new EquippedItem(passive, EquipmentSlot.ANY));

		return list;
	}

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (InventoryAPI.isRPGInventory(event.getInventory()))
			PlayerData.get((Player) event.getPlayer()).updateInventory();
	}
}
