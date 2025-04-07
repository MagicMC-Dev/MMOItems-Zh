package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItemImpl;
import net.Indyuce.mmoitems.inventory.InventoryResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @see net.Indyuce.mmoitems.api.event.inventory.ItemEquipEvent
 * @see net.Indyuce.mmoitems.api.event.inventory.ItemUnequipEvent
 * @deprecated
 */
@Deprecated
public class RefreshInventoryEvent extends Event {

    @NotNull final List<EquippedItem> itemsToEquip;
    @Deprecated
    @NotNull public List<EquippedItem> getItemsToEquip() { return itemsToEquip; }

    @NotNull
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Deprecated
    public PlayerData getPlayerData() {
        return playerData;
    }

    @NotNull final Player player;
    @NotNull final PlayerData playerData;

    @Deprecated
    public RefreshInventoryEvent(@NotNull InventoryResolver resolver) {
        this.itemsToEquip = resolver.getEquipped().stream().map(EquippedItemImpl::new).collect(Collectors.toList());
        this.playerData = resolver.getPlayerData();
        this.player = playerData.getPlayer();
    }

    @NotNull static final HandlerList handlers = new HandlerList();
    @Deprecated
    @NotNull public HandlerList getHandlers() { return handlers; }
    @Deprecated
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
