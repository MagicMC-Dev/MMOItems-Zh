package net.Indyuce.mmoitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.player.PlayerData;

public class ItemEquipEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled = false;
	private PlayerData playerData;
	private ItemStack item;

	public ItemEquipEvent(Player player, ItemStack item) {
		super(player);
		this.item = item;
		this.playerData = PlayerData.get(player);
	}

	public ItemStack getItem() {
		return item;
	}

	public void setCancelled(boolean bool) {
		cancelled = bool;
	}
	
	public PlayerData getPlayerData() {
		return playerData;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
