package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class EquippedItem {
    private final NBTItem item;
    private final EquipmentSlot slot;

    private VolatileMMOItem cached;

    /**
     * An item equipped by a player in a specific slot
     *
     * @param item The item equipped
     * @param slot Slot in which the item is placed
     */
    public EquippedItem(ItemStack item, EquipmentSlot slot) {
        this(NBTItem.get(item), slot);
    }

    /**
     * An item equipped by a player in a specific slot
     *
     * @param item The item equipped
     * @param slot Slot in which the item is placed
     */
    public EquippedItem(NBTItem item, EquipmentSlot slot) {
        this.item = item;
        this.slot = slot;
    }

    public VolatileMMOItem getCached() {
        return Objects.requireNonNull(cached, "Item not cached yet");
    }

    public void cacheItem() {
        Validate.isTrue(cached == null, "MMOItem has already been cached");
        cached = new VolatileMMOItem(item);
    }

    public NBTItem getNBT() {
        return item;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    /**
     * This is a small optimization which reduces the amount of items
     * taken into account by the MMOItems player inventory handler.
     *
     * @return If item placement is legal
     */
    public boolean isPlacementLegal() {

        // Vanilla items are ignored
        final @Nullable String typeFormat = item.getString("MMOITEMS_ITEM_TYPE");
        if (typeFormat == null)
            return false;

        final @Nullable Type type = MMOItems.plugin.getTypes().get(typeFormat);
        if (type == null)
            return false;

        final ModifierSource modSource = type.getModifierSource();
        return EquipmentSlot.OFF_HAND.isCompatible(modSource, slot) || EquipmentSlot.MAIN_HAND.isCompatible(modSource, slot);
    }

    /**
     * Allows editing the item, wherever it is that it is
     * currently equipped, due to stats like {@link ItemStats#DOWNGRADE_ON_DEATH}
     * that target equipped items.
     *
     * @param item Item to replace in the current slot
     */
    public abstract void setItem(@Nullable ItemStack item);
}