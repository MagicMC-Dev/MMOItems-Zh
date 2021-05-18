package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * When a player eats a {@link net.Indyuce.mmoitems.stat.type.SelfConsumable}
 *
 * @author Gunging
 */
public class ConsumableConsumedEvent extends Event implements Cancellable {

    @NotNull final VolatileMMOItem mmoitem;

    @NotNull
    public VolatileMMOItem getMMOItem() {
        return mmoitem;
    }

    @NotNull final Player player;
    @NotNull
    public Player getPlayer() {
        return player;
    }
    @NotNull final Consumable useItem;
    @NotNull
    public Consumable getUseItem() {
        return useItem;
    }


    boolean cancelled = false;


    /**
     * @return        If the consumable will be consumed.
     *                <br>
     *                <code>null</code> is the default, and means that it will be consumed if it successfully
     *                performs any {@link net.Indyuce.mmoitems.stat.type.SelfConsumable#onSelfConsume(VolatileMMOItem, Player)},
     *                any other value will override this and allows you to consume consumables that did no action, or
     *                prevent consumables from being consumed even though they executed actions.
     */
    @Nullable public Boolean isConsume() {
        return consume;
    }

    /**
     * @param consume If the consumable will be consumed.
     *                <br>
     *                <code>null</code> is the default, and means that it will be consumed if it successfully
     *                performs any {@link net.Indyuce.mmoitems.stat.type.SelfConsumable#onSelfConsume(VolatileMMOItem, Player)},
     *                any other value will override this and allows you to consume consumables that did no action, or
     *                prevent consumables from being consumed even though they executed actions.
     */
    public void setConsume(@Nullable Boolean consume) {
        this.consume = consume;
    }

    @Nullable Boolean consume = null;

    public ConsumableConsumedEvent(@NotNull VolatileMMOItem mmo, @NotNull Player player, @NotNull Consumable useItem) {
        this.mmoitem = mmo;
        this.player = player;
        this.useItem = useItem;
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean b) { cancelled = b; }

    private static final HandlerList handlers = new HandlerList();
    @NotNull @Override public HandlerList getHandlers() { return handlers; }
}
