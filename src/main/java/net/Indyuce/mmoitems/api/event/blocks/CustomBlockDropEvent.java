package net.Indyuce.mmoitems.api.event.blocks;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CustomBlockDropEvent extends PlayerDataEvent implements Cancellable {
	private final CustomBlock block;
	private final List<ItemStack> drops;

	private boolean cancelled;

	private static final HandlerList handlers = new HandlerList();

	public CustomBlockDropEvent(PlayerData playerData, CustomBlock block, List<ItemStack> drops) {
		super(playerData);

		this.block = block;
		this.drops = drops;
	}

	public CustomBlock getCustomBlock() {
		return block;
	}

	public List<ItemStack> getDrops() {
		return drops;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
