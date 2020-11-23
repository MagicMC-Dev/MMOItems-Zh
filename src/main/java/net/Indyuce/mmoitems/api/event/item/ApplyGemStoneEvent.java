package net.Indyuce.mmoitems.api.event.item;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class ApplyGemStoneEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final VolatileMMOItem gemStone;
	private final MMOItem targetItem;

	/**
	 * Called when a player tries to apply a gem stone onto an item
	 * 
	 * @param playerData
	 *            Player applying the gem stone
	 * @param gemStone
	 *            Gem stone being applied
	 * @param targetItem
	 *            Item on which the gem is being applied
	 */
	public ApplyGemStoneEvent(PlayerData playerData, VolatileMMOItem gemStone, MMOItem targetItem) {
		super(playerData);

		this.gemStone = gemStone;
		this.targetItem = targetItem;
	}

	public VolatileMMOItem getGemStone() {
		return gemStone;
	}

	public MMOItem getTargetItem() {
		return targetItem;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
