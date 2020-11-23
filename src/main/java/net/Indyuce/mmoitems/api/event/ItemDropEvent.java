package net.Indyuce.mmoitems.api.event;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemDropEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled = false;
	private final DropCause cause;
	private final List<ItemStack> drops;
	private final LivingEntity player;

	// data that depends on drop cause
	private final Block block;
	private final Entity entity;
	private final String mythicMobName;

	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, Block block) {
		this(player, drops, cause, block, null, null);
	}

	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, Entity entity) {
		this(player, drops, cause, null, entity, null);
	}

	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, String mythicMobName) {
		this(player, drops, cause, null, null, mythicMobName);
	}

	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, Block block, Entity entity, String mythicMobName) {
		this.player = player;
		this.cause = cause;
		this.drops = drops;

		this.block = block;
		this.entity = entity;
		this.mythicMobName = mythicMobName;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public List<ItemStack> getDrops() {
		return drops;
	}

	public DropCause getCause() {
		return cause;
	}

	public LivingEntity getWhoDropped() {
		return player;
	}

	public Block getMinedBlock() {
		return block;
	}

	public Entity getKilledEntity() {
		return entity;
	}

	public String getKilledMythicMobName() {
		return mythicMobName;
	}

	public void setCancelled(boolean bool) {
		cancelled = bool;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum DropCause {
		BLOCK,
		MONSTER,
		MYTHIC_MOB
	}
}
