package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class UnsocketGemStoneEvent extends PlayerDataEvent {
    private static final HandlerList handlers = new HandlerList();

    private final VolatileMMOItem consumable;
    private final MMOItem targetItem;

    /**
     * Called when a player tries to un-socket a gem stone onto an item, default via consumable item
     *
     * @param playerData Player un-applying the gem stone
     * @param consumable Consumable item being applied
     * @param targetItem Item on which the consumable item is being applied
     */
    public UnsocketGemStoneEvent(PlayerData playerData, VolatileMMOItem consumable, MMOItem targetItem) {
        super(playerData);

        this.consumable = consumable;
        this.targetItem = targetItem;
    }

    public VolatileMMOItem getConsumable() {
        return consumable;
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
