package net.Indyuce.mmoitems.api.event.item;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class IdentifyItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final VolatileMMOItem consumable;
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
	public IdentifyItemEvent(PlayerData playerData, VolatileMMOItem consumable, NBTItem unidentified) {
		super(playerData);

		this.consumable = consumable;
		this.unidentified = unidentified;
	}

	public VolatileMMOItem getConsumable() {
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
