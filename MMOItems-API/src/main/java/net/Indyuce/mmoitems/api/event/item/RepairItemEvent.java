package net.Indyuce.mmoitems.api.event.item;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.event.HandlerList;

/**
 * When a consumable is used to repair a VANILLA item.
 */
public class RepairItemEvent extends PlayerDataEvent {
    private static final HandlerList handlers = new HandlerList();

    private final VolatileMMOItem consumable;
    private final NBTItem target;

    private int repaired;

    /**
     * Called when a player repairs an item using a consumable
     *
     * @param playerData Player repairing the item
     * @param consumable Consumable used to repair the item
     * @param target     Item being repaired
     * @param repaired   Amount of durability being repaired
     */
    public RepairItemEvent(PlayerData playerData, VolatileMMOItem consumable, NBTItem target, int repaired) {
        super(playerData);

        this.consumable = consumable;
        this.target = target;
        this.repaired = repaired;
    }

    public VolatileMMOItem getConsumable() {
        return consumable;
    }

    public NBTItem getTargetItem() {
        return target;
    }

    public int getRepaired() {
        return repaired;
    }

    @Deprecated
    public double getRepairedPercent() {
        final boolean customWeapon = target.hasTag("MMOITEMS_DURABILITY");
        final double maxDurability = customWeapon ? target.getDouble("MMOITEMS_MAX_DURABILITY") : target.getItem().getType().getMaxDurability();
        return (double) getRepaired() / maxDurability;
    }

    public void setRepaired(int repaired) {
        this.repaired = Math.max(0, repaired);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
