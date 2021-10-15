package net.Indyuce.mmoitems.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemBuildEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private ItemStack itemStack;
	private boolean cancelled;

	public ItemBuildEvent(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ItemBuildEvent setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		return this;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
