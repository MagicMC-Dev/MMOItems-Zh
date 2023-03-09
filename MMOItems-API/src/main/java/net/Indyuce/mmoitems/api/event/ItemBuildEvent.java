package net.Indyuce.mmoitems.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Will be removed in the future. SCRIPTS must replace these
 *         because there are  no events which can support both sync and async
 */
@Deprecated
public class ItemBuildEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private ItemStack itemStack;

    public ItemBuildEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemBuildEvent setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    /**
     * @deprecated See {@link #setCancelled(boolean)}
     */
    @Deprecated
    public boolean isCancelled() {
        return itemStack == null;
    }

    /**
     * @deprecated Set the generated item stack to null instead. Cancelling
     *         the build event does not cancel the process that generated the item
     */
    @Deprecated
    public void setCancelled(boolean cancel) {
        if (cancel)
            itemStack = null;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
