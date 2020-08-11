package net.Indyuce.mmoitems.api.event.item;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.item.NBTItem;

public class IdentifyItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final MMOItem consumable;
	private final NBTItem unidentified;

	/**
	 * Called when a player tries to identify an item using a consumable
	 * 
	 * @param playerData
	 *            Player identifying the item
	 * @param consumable
	 *            Consumable used to identify the item
	 * @param unidentified
	 *            Item being identified
	 */
	public IdentifyItemEvent(PlayerData playerData, MMOItem consumable, NBTItem unidentified) {
		super(playerData);

		this.consumable = consumable;
		this.unidentified = unidentified;
	}

	public MMOItem getConsumable() {
		return consumable;
	}

	public NBTItem getUnidentifiedItem() {
		return unidentified;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
