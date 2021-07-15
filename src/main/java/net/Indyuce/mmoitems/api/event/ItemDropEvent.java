package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.block.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemDropEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	private final DropCause cause;
	private final List<ItemStack> drops;
	private final LivingEntity player;

	// Data that depends on drop cause
	private final Block block;
	private final Entity entity;
	private final String mythicMobName;
	private final CustomBlock customBlock;

	/**
	 * When an item drops from a custom block
	 *
	 * @param player      Player dropping the item
	 * @param drops       Item drops
	 * @param customBlock Custom block broken
	 */
	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, CustomBlock customBlock) {
		this(player, drops, DropCause.CUSTOM_BLOCK, null, null, null, customBlock);
	}

	/**
	 * When an item drops from a normal block
	 *
	 * @param player Player dropping the item
	 * @param drops  Item drops
	 * @param block  Normal block broken
	 */
	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, Block block) {
		this(player, drops, DropCause.NORMAL_BLOCK, block, null, null, null);
	}

	/**
	 * When an item drops from a dying entity
	 *
	 * @param player Player dropping the item
	 * @param drops  Item drops
	 * @param entity Entity being killed
	 */
	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, Entity entity) {
		this(player, drops, DropCause.NORMAL_MONSTER, null, entity, null, null);
	}

	/**
	 * When an item drops from a MythicMobs mob
	 *
	 * @param player        Player dropping the item
	 * @param drops         Item drops
	 * @param mythicMobName Internal id of the mythic mob
	 */
	@Deprecated
	public ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, String mythicMobName) {
		this(player, drops, DropCause.MYTHIC_MOB, null, null, mythicMobName, null);
	}

	private ItemDropEvent(LivingEntity player, List<ItemStack> drops, DropCause cause, Block block, Entity entity, String mythicMobName, CustomBlock customBlock) {
		this.player = player;
		this.cause = cause;
		this.drops = drops;

		this.block = block;
		this.entity = entity;
		this.mythicMobName = mythicMobName;
		this.customBlock = customBlock;
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

		/**
		 * Item dropped from a non custom block
		 */
		NORMAL_BLOCK,

		/**
		 * Item dropped from a non MythicMobs mob
		 */
		NORMAL_MONSTER,

		/**
		 * Item dropped from a MythicMobs mob
		 */
		@Deprecated
		MYTHIC_MOB,

		/**
		 * Item dropped from a custom block
		 */
		CUSTOM_BLOCK;
	}
}
