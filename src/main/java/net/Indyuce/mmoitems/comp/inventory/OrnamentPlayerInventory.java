package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells MMOItems where to find additional equipment.
 * <p></p>
 * Ornaments - Found in any inventory slot.
 */
public class OrnamentPlayerInventory implements PlayerInventory, Listener {
	public OrnamentPlayerInventory() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		// Ornaments
		for (ItemStack item : player.getInventory().getContents()) {
			NBTItem nbtItem;
			if (item != null && (nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item)).hasType() && Type.get(nbtItem.getType()).getEquipmentType() == EquipmentSlot.ANY)
				list.add(new EquippedItem(nbtItem, EquipmentSlot.ANY));
		}

		return list;
	}

	@EventHandler(ignoreCancelled = true)
	public void a(EntityPickupItemEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			NBTItem nbt = NBTItem.get(event.getItem().getItemStack());
			if (nbt.hasType() && Type.get(nbt.getType()).getEquipmentType() == EquipmentSlot.ANY)
				PlayerData.get((Player) event.getEntity()).updateInventory();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void b(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if (nbt.hasType() && Type.get(nbt.getType()).getEquipmentType() == EquipmentSlot.ANY)
			PlayerData.get(event.getPlayer()).updateInventory();
	}
}
