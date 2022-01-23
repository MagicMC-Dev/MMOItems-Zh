package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class EditableEquippedItem extends EquippedItem {
    public EditableEquippedItem(ItemStack item, EquipmentSlot slot) {
        super(item, slot);
    }

    public EditableEquippedItem(NBTItem item, EquipmentSlot slot) {
        super(item, slot);
    }

    /**
     * Allows editing the item, wherever it is that it is
     * currently equipped, due to stats like
     * {@link net.Indyuce.mmoitems.ItemStats#DOWNGRADE_ON_DEATH}
     * that target equipped items.
     *
     * @param item Item to replace in the current slot
     */
    public abstract void setItem(@Nullable ItemStack item);
}
