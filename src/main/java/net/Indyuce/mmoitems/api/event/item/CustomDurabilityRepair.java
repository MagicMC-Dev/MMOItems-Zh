package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomDurabilityRepair extends Event implements Cancellable {

    @NotNull DurabilityItem sourceItem;
    int durabilityIncrease;

    boolean cancelled = false;

    public CustomDurabilityRepair(@NotNull DurabilityItem item, int impendingRepair) {
        sourceItem = item;
        durabilityIncrease = impendingRepair; }

    public int getDurabilityIncrease() { return durabilityIncrease; }
    @NotNull public DurabilityItem getSourceItem() { return sourceItem; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean b) { cancelled = b; }

    private static final HandlerList handlers = new HandlerList();
    @NotNull @Override public HandlerList getHandlers() { return handlers; }
}
