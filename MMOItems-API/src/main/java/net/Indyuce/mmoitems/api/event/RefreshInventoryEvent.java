package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RefreshInventoryEvent extends Event {

    @NotNull final List<EquippedItem> itemsToEquip;
    @NotNull public List<EquippedItem> getItemsToEquip() { return itemsToEquip; }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public PlayerData getPlayerData() {
        return playerData;
    }

    @NotNull final Player player;
    @NotNull final PlayerData playerData;

    public RefreshInventoryEvent(@NotNull List<EquippedItem> itemsToEquip, @NotNull Player player, @NotNull PlayerData playerData) {
        this.itemsToEquip = itemsToEquip;
        this.player = player;
        this.playerData = playerData;
    }

    @NotNull static final HandlerList handlers = new HandlerList();
    @NotNull public HandlerList getHandlers() { return handlers; }
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
