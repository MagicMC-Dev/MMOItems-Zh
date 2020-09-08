package net.Indyuce.mmoitems.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.player.PlayerData;

public class CraftMMOItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private ItemStack stack;
	
	public CraftMMOItemEvent(PlayerData playerData, ItemStack stack) {
		super(playerData);
		this.stack = stack;
	}
	
	public ItemStack getResult() {
		return stack;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
