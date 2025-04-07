package net.Indyuce.mmoitems.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUpdate {

    /**
     * Used to apply new player modifiers
     */
    @Nullable
    private final EquippedItem newItem;

    /**
     * Used to clear out old player modifiers
     */
    @Nullable
    private final EquippedItem previousItem;

    private final EquipmentSlot equipmentSlot;

    public ItemUpdate(@NotNull EquipmentSlot equipmentSlot, @Nullable EquippedItem previousItem, @Nullable EquippedItem newItem) {
        this.equipmentSlot = equipmentSlot;
        this.newItem = newItem;
        this.previousItem = previousItem;
    }

    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    @Nullable
    public EquippedItem getNew() {
        return newItem;
    }

    @Nullable
    public EquippedItem getOld() {
        return previousItem;
    }
}
