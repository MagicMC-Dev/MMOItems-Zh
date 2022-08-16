package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.api.InventoryAPI;

import java.util.ArrayList;
import java.util.List;

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
				list.add(new EquippedItem(passive, EquipmentSlot.ACCESSORY));

		return list;
	}

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (InventoryAPI.isRPGInventory(event.getInventory()))
			PlayerData.get((Player) event.getPlayer()).updateInventory();
	}
}
