package net.Indyuce.mmoitems.api.event.blocks;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.CustomBlock;

public class CustomBlockDropEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final CustomBlock custom;
	private final ItemStack drop;

	public CustomBlockDropEvent(Player p, CustomBlock c, ItemStack d) {
		super(p); custom = c; drop = d;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
	
	public CustomBlock getCustomBlock() {
		return custom;
	}

	public ItemStack getDrop() {
		return drop;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
