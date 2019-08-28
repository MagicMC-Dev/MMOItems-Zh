package net.Indyuce.mmoitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class ItemDurabilityStateChangeEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled = false;
	private ItemStack item;
	private String oldState, newState;

	public ItemDurabilityStateChangeEvent(Player player, ItemStack item, String oldState, String newState) {
		super(player);
		this.item = item;
		this.oldState = oldState;
		this.newState = newState;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean bool) {
		cancelled = bool;
	}

	public ItemStack getItem() {
		return item;
	}

	public String getOldState() {
		return oldState;
	}

	public String getNewState() {
		return newState;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
