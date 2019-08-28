package net.Indyuce.mmoitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import net.Indyuce.mmoitems.api.item.NBTItem;

public class ItemBreakEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	private NBTItem item;
	private boolean itemBreak;

	public ItemBreakEvent(Player player, NBTItem item) {
		super(player);
		this.item = item;
		this.itemBreak = item.getBoolean("MMOITEMS_WILL_BREAK");
	}

	public NBTItem getItem() {
		return item;
	}

	/*
	 * returns if the item really broke or if it just became unusable till a
	 * player repairs it
	 */
	public boolean doesItemBreak() {
		return itemBreak;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
