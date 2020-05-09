package net.Indyuce.mmoitems.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.Indyuce.mmoitems.MMOItems;

public abstract class PluginInventory implements InventoryHolder {
	protected final Player player;

	protected int page = 1;

	public PluginInventory(Player player) {
		this.player = player;
	}

	public int getPage() {
		return page;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public abstract Inventory getInventory();

	public abstract void whenClicked(InventoryClickEvent event);

	/*
	 * since 1.14 sync events cannot be called in async methods anymore so MI
	 * has to register a delayed sync task to open the inventory again
	 */
	public void open() {
		if (Bukkit.isPrimaryThread())
			getPlayer().openInventory(getInventory());
		else
			Bukkit.getScheduler().runTask(MMOItems.plugin, () -> getPlayer().openInventory(getInventory()));
	}
}
