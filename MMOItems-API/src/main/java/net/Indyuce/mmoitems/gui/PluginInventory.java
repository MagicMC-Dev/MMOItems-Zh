package net.Indyuce.mmoitems.gui;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class PluginInventory implements InventoryHolder {
	protected final PlayerData playerData;
	protected final Player player;

	protected int page = 1;

	public PluginInventory(@NotNull Player player) {
		this(PlayerData.get(player));
	}

	public PluginInventory(PlayerData playerData) {
		this.playerData = playerData;
		this.player = playerData.getPlayer();
	}

	public int getPage() {
		return page;
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	@Override
	public abstract Inventory getInventory();

	public abstract void whenClicked(InventoryClickEvent event);

	/*
	 * since 1.14 sync events cannot be called in async methods anymore so MI
	 * has to register a delayed sync task to open the inventory again sad
	 */
	public void open() {
		if (Bukkit.isPrimaryThread())
			player.openInventory(getInventory());
		else
			Bukkit.getScheduler().runTask(MMOItems.plugin, () -> player.openInventory(getInventory()));
	}
}
