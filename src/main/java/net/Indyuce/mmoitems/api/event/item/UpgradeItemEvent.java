package net.Indyuce.mmoitems.api.event.item;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;

public class UpgradeItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final VolatileMMOItem consumable;
	private final MMOItem target;
	private final UpgradeData consumableData, targetData;

	/**
	 * Called when a player upgrades an item using a consumable
	 * 
	 * @param playerData
	 *            Player upgrading the item
	 * @param consumable
	 *            Consumable used to upgrade the item
	 * @param target
	 *            Item being upgraded
	 * @param consumableData
	 *            Upgrade info about the consumable
	 * @param targetData
	 *            Upgrade info about the target item
	 */
	public UpgradeItemEvent(PlayerData playerData, VolatileMMOItem consumable, MMOItem target, UpgradeData consumableData, UpgradeData targetData) {
		super(playerData);

		this.consumable = consumable;
		this.target = target;
		this.consumableData = consumableData;
		this.targetData = targetData;
	}

	public VolatileMMOItem getConsumable() {
		return consumable;
	}

	public MMOItem getTargetItem() {
		return target;
	}

	public UpgradeData getConsumableData() {
		return consumableData;
	}

	public UpgradeData getTargetData() {
		return targetData;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
