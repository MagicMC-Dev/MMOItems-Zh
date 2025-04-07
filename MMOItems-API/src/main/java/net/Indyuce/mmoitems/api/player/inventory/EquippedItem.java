package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

@Deprecated
public abstract class EquippedItem {
    private final net.Indyuce.mmoitems.inventory.EquippedItem wrapped;

    @Deprecated
    public EquippedItem(net.Indyuce.mmoitems.inventory.EquippedItem wrapped) {
        this.wrapped = wrapped;
    }

    @Deprecated
    public VolatileMMOItem getCached() {
        return wrapped.reader();
    }

    @Deprecated
    public NBTItem getNBT() {
        return wrapped.getItem();
    }

    @Deprecated
    public EquipmentSlot getSlot() {
        return wrapped.getEquipmentSlot();
    }

    @Deprecated
    public boolean isPlacementLegal() {
        return wrapped.isPlacementLegal();
    }

    @Deprecated
    public abstract void setItem(@Nullable ItemStack item);
}