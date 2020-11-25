package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class SoulbindingListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	private void itemPickup(EntityPickupItemEvent e) {
		if (!e.getEntity().getType().equals(EntityType.PLAYER) || isDisabled("pickup"))
			return;
		ItemStack item = e.getItem().getItemStack();
		if (!isValid(item))
			return;
		e.getItem().setItemStack(getSoulboundItem((Player) e.getEntity(), item));
	}

	@EventHandler(ignoreCancelled = true)
	private void itemCraft(CraftItemEvent e) {
		if (!isDisabled("craft") && isValid(e.getCurrentItem()))
			e.setCurrentItem(getSoulboundItem((Player) e.getWhoClicked(), e.getCurrentItem()));
	}

	@EventHandler(ignoreCancelled = true)
	private void inventoryMove(InventoryClickEvent e) {
		if(!isDisabled("click") && isValid(e.getCurrentItem()))
			e.setCurrentItem(getSoulboundItem((Player) e.getWhoClicked(), e.getCurrentItem()));
	}

	@EventHandler(ignoreCancelled = true)
	public void dropItem(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if(!MMOItems.plugin.getConfig().getBoolean("soulbound.can-drop")
				&& nbt.hasTag("MMOITEMS_SOULBOUND")) event.setCancelled(true);
	}

	private boolean isValid(ItemStack item) {
		NBTItem nbt = NBTItem.get(item);
		return nbt.hasType() && nbt.getBoolean("MMOITEMS_AUTO_SOULBIND") && !nbt.hasTag("MMOITEMS_SOULBOUND");
	}

	private boolean isDisabled(String type) {
		return MMOItems.plugin.getConfig().getBoolean("soulbound.auto-bind.disable-on-" + type);
	}

	private ItemStack getSoulboundItem(Player p, ItemStack item) {
		LiveMMOItem mmoItem = new LiveMMOItem(item);
		mmoItem.setData(ItemStats.SOULBOUND, ((Soulbound) ItemStats.SOULBOUND).newSoulboundData(p.getUniqueId(),
				p.getName(), MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1)));
		return mmoItem.newBuilder().build();
	}
}
