package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * When a player eats a consumable
 *
 * @author Gunging
 */
public class ConsumableConsumedEvent extends PlayerDataEvent {
    @NotNull
    private final VolatileMMOItem mmoitem;
    @NotNull
    private final Consumable useItem;

    @Nullable
    private boolean consumed = true;

    private static final HandlerList handlers = new HandlerList();

    public ConsumableConsumedEvent(@NotNull PlayerData player, @NotNull VolatileMMOItem mmo, @NotNull Consumable useItem) {
        super(player);

        this.mmoitem = mmo;
        this.useItem = useItem;
    }

    /**
     * @deprecated Use the other constructor instead
     */
    @Deprecated
    public ConsumableConsumedEvent(@NotNull VolatileMMOItem mmo, @NotNull Player player, @NotNull Consumable useItem) {
        super(PlayerData.get(player));

        this.mmoitem = mmo;
        this.useItem = useItem;
    }

    @NotNull
    public VolatileMMOItem getMMOItem() {
        return mmoitem;
    }

    @NotNull
    public Consumable getUseItem() {
        return useItem;
    }

    /**
     * @deprecated Use {@link ConsumableConsumedEvent#isConsumed()} instead
     */
    @Deprecated
    public Boolean isConsume() {
        return (Boolean) consumed;
    }

    /**
     * @return If the consumable will be consumed. If the event is
     * canceled, the item will not be consumed whatever happens.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * @deprecated Use {@link ConsumableConsumedEvent#setConsumed(boolean)} instead
     */
    public void setConsume(@Nullable Boolean consume) {
        this.consumed = consume;
    }

    /**
     * @param consumed If the consumable will be consumed.
     *                 <br>
     *                 <code>null</code> is the default, and means that it will be consumed if it successfully
     *                 performs any {@link PlayerConsumable#onConsume(VolatileMMOItem, Player)},
     *                 any other value will override this and allows you to consume consumables that did no action, or
     *                 prevent consumables from being consumed even though they executed actions.
     */
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
