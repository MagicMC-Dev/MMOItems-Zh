package net.Indyuce.mmoitems.api.event;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class GemStoneAppliedEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final MMOItem gemStone, targetItem;

	/**
	 * Called when a player tries to apply a gem stone onto an item
	 * 
	 * @param playerData
	 *            Player applying the gem stone
	 * @param gemStone
	 *            Gem stone being applied
	 * @param target
	 *            Item on which the gem is being applied
	 */
	public GemStoneAppliedEvent(PlayerData playerData, MMOItem gemStone, MMOItem targetItem) {
		super(playerData);

		this.gemStone = gemStone;
		this.targetItem = targetItem;
	}

	public MMOItem getGemStone() {
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
