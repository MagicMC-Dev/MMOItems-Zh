package net.Indyuce.mmoitems.api.event.inventory;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.EquippedItem;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ItemUnequipEvent extends PlayerDataEvent {
    private final EquippedItem item;

    private static final HandlerList HANDLERS = new HandlerList();

    public ItemUnequipEvent(PlayerData playerData, EquippedItem item) {
        super(playerData);

        this.item = item;
    }

    @NotNull
    public EquippedItem getItem() {
        return item;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
