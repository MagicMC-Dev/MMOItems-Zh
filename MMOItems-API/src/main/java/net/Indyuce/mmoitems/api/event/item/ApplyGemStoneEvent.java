package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

public class ApplyGemStoneEvent extends PlayerDataEvent {
    private static final HandlerList handlers = new HandlerList();

    private final VolatileMMOItem gemStone;
    private final MMOItem targetItem;

    private GemStone.ResultType result;

    /**
     * Called when a player tries to apply a gem stone onto an item
     *
     * @param playerData Player applying the gem stone
     * @param gemStone   Gem stone being applied
     * @param targetItem Item on which the gem is being applied
     * @param result     See {@link GemStone.ResultType}
     */
    public ApplyGemStoneEvent(PlayerData playerData, VolatileMMOItem gemStone, MMOItem targetItem, GemStone.ResultType result) {
        super(playerData);

        this.gemStone = gemStone;
        this.targetItem = targetItem;
        this.result = result;
    }

    public VolatileMMOItem getGemStone() {
        return gemStone;
    }

    public MMOItem getTargetItem() {
        return targetItem;
    }

    public GemStone.ResultType getResult() {
        return result;
    }

    public void setResult(GemStone.ResultType result) {
        Validate.notNull(result, "Result cannot be null");
        this.result = result;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
