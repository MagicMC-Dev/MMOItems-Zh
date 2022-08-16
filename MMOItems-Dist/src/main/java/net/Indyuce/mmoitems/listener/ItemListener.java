package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.listener.reforging.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemListener implements Listener {

	// Aye
	public ItemListener() {
		//RFG//MMOItems.log(" §b>§a>§e> §7Registering Listeners");

		// Register Reforger Listeners
		Bukkit.getPluginManager().registerEvents(new RFGKeepName(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepLore(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepEnchantments(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepExternalSH(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepGems(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepModifications(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepSoulbound(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepUpgrades(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFGKeepRNG(), MMOItems.plugin);

		// Amount ones
		Bukkit.getPluginManager().registerEvents(new RFGKeepDurability(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFFKeepAmount(), MMOItems.plugin);
		Bukkit.getPluginManager().registerEvents(new RFFKeepSkins(), MMOItems.plugin);

	}

	@EventHandler(ignoreCancelled = true)
	private void itemPickup(EntityPickupItemEvent e) {
		if (!e.getEntity().getType().equals(EntityType.PLAYER)) return;

		ItemStack newItem = modifyItem(e.getItem().getItemStack(), (Player) e.getEntity(), "pickup");
		if (newItem != null) e.getItem().setItemStack(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void itemCraft(CraftItemEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		ItemStack newItem = modifyItem(e.getCurrentItem(), (Player) e.getWhoClicked(), "craft");
		if (newItem != null) e.setCurrentItem(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void inventoryMove(InventoryClickEvent e) {
		if (e.getInventory().getType() != InventoryType.CRAFTING || !(e.getWhoClicked() instanceof Player)) return;
		ItemStack newItem = modifyItem(e.getCurrentItem(), (Player) e.getWhoClicked(), "click");
		if (newItem != null) e.setCurrentItem(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void dropItem(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if (!MMOItems.plugin.getConfig().getBoolean("soulbound.can-drop") && nbt.hasTag("MMOITEMS_SOULBOUND"))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		ItemStack newItem = modifyItem(player.getEquipment().getHelmet(), player, "join");
		if(newItem != null) player.getEquipment().setHelmet(newItem);
		newItem = modifyItem(player.getEquipment().getChestplate(), player, "join");
		if(newItem != null) player.getEquipment().setChestplate(newItem);
		newItem = modifyItem(player.getEquipment().getLeggings(), player, "join");
		if(newItem != null) player.getEquipment().setLeggings(newItem);
		newItem = modifyItem(player.getEquipment().getBoots(), player, "join");
		if(newItem != null) player.getEquipment().setBoots(newItem);

		for (int j = 0; j < 9; j++) {
			newItem = modifyItem(player.getInventory().getItem(j), player, "join");
			if (newItem != null) player.getInventory().setItem(j, newItem);
		}

		newItem = modifyItem(player.getEquipment().getItemInOffHand(), player, "join");
		if(newItem != null) player.getEquipment().setItemInOffHand(newItem);
	}

	@Nullable private ItemStack modifyItem(@Nullable ItemStack stack, @NotNull Player player, @NotNull String reason) {
		//RFG//MMOItems.log("§8Reforge §cMOD§7 Modifying " + SilentNumbers.getItemName(stack) + " §7due to§3 " + reason);

		// Sleep on metaless stacks
		if (stack == null) { return null; }
		if (!stack.hasItemMeta()) { return null; }

		// Create a reforger to look at it
		MMOItemReforger mod = new MMOItemReforger(stack);

		// Shouldn't update? I sleep
		if (!mod.shouldReforge(reason)) { return null; }

		// All right update then
		mod.setPlayer(player);
		if (!mod.reforge(MMOItems.plugin.getLanguage().revisionOptions)) {

			return null; }

		// Drop all those items
		for (ItemStack drop : player.getInventory().addItem(
				mod.getReforgingOutput().toArray(new ItemStack[0])).values()) {

			// Not air right
			if (SilentNumbers.isAir(drop)) { continue; }

			// Drop to the world
			player.getWorld().dropItem(player.getLocation(), drop); }

		// That's it
		return mod.getResult();
	}
}
