package net.Indyuce.mmoitems.api.event.item;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.item.NBTItem;

public class BreakSoulboundEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final MMOItem consumable;
	private final NBTItem target;

	/**
	 * Called when a player tries to break an item soulbound
	 * 
	 * @param playerData
	 *            Player breaking the soulbound
	 * @param consumable
	 *            Consumable used to break the soulbound
	 * @param target
	 *            Item with soulbound
	 */
	public BreakSoulboundEvent(PlayerData playerData, MMOItem consumable, NBTItem target) {
		super(playerData);

		this.consumable = consumable;
		this.target = target;
	}

	public MMOItem getConsumable() {
		return consumable;
	}

	public NBTItem getTargetItem() {
		return target;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
