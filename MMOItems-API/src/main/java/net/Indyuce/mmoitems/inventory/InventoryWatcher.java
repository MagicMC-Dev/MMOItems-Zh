package net.Indyuce.mmoitems.inventory;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public interface InventoryWatcher {

    public default ItemUpdate watchSingle(@NotNull EquipmentSlot slot) {
        return watchSingle(slot, 0, Optional.empty());
    }

    public default ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int index) {
        return watchSingle(slot, index, Optional.empty());
    }

    public default ItemUpdate watchSingle(@NotNull EquipmentSlot slot, @NotNull Optional<ItemStack> newItem) {
        return watchSingle(slot, 0, newItem);
    }

    /**
     * @param slot    Slot to update
     * @param index   Index of slot, if required (ornaments and accessories)
     * @param newItem New item if this function is called ahead of time
     * @return Nothing if the watcher did not detect any change, or the UUID of the unregistered
     *         item, which can in turn be used by the inventory checker, to filter out old buffs.
     */
    @Nullable
    public ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int index, @NotNull Optional<ItemStack> newItem);

    public void watchAll(@NotNull Consumer<ItemUpdate> callback);

    @Nullable
    static ItemUpdate checkForUpdate(@Nullable ItemStack newItem, @Nullable EquippedItem existing, @NotNull EquipmentSlot slot) {
        return checkForUpdate(newItem, existing, slot, 0);
    }

    @Nullable
    static ItemUpdate checkForUpdate(@Nullable ItemStack newItem, @Nullable EquippedItem existing, @NotNull EquipmentSlot slot, int index) {

        // Current item is non-existent
        if (existing == null) {

            // null->null (no update required)
            if (UtilityMethods.isAir(newItem)) return null;

            // null->some
            return new ItemUpdate(slot, null, new EquippedItem(slot, index, NBTItem.get(newItem)));
        }

        // some->null
        if (UtilityMethods.isAir(newItem)) return new ItemUpdate(slot, existing, null);

        // some->same (no update required)
        /*
         * To drastically speed up performance instead of relying on
         * accurate ItemStack#isSimilar(ItemStack) calls, we take the
         * hypothesis that different hashes mean different items.
         * There's actually a E-32 chance that it is not the case,
         * but it is considered to be sufficiently small enough.
         */
        if (existing.getItemHash() == newItem.hashCode()) return null;

        // some->some
        return new ItemUpdate(slot, existing, new EquippedItem(slot, index, NBTItem.get(newItem)));
    }

    @NotNull
    static Optional<ItemStack> optionalOf(@Nullable ItemStack stack) {
        return Optional.of(stack == null ? new ItemStack(Material.AIR) : stack);
    }

    static void callbackIfNotNull(@Nullable ItemUpdate update, @NotNull Consumer<ItemUpdate> callback) {
        if (update != null) callback.accept(update);
    }
}
