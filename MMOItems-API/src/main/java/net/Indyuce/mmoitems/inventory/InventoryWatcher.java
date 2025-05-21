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

@Deprecated
public abstract class InventoryWatcher {

    @Nullable
    public ItemUpdate watchInventory(int index, @NotNull Optional<ItemStack> newItem) {
        return null;
    }

    @Nullable
    public ItemUpdate watchVanillaSlot(@NotNull EquipmentSlot slot, @NotNull Optional<ItemStack> newItem) {
        return null;
    }

    public abstract void watchAll(@NotNull Consumer<ItemUpdate> callback);

    @Nullable
    protected ItemUpdate checkForUpdate(@Nullable ItemStack newItem, @Nullable EquippedItem existing, @NotNull EquipmentSlot slot) {
        return checkForUpdate(newItem, existing, slot, 0, 0);
    }

    @Nullable
    protected ItemUpdate checkForUpdate(@Nullable ItemStack newItem, @Nullable EquippedItem existing, @NotNull EquipmentSlot slot, int slotIndex) {
        return checkForUpdate(newItem, existing, slot, slotIndex, 0);
    }

    @Nullable
    protected ItemUpdate checkForUpdate(@Nullable ItemStack newItem, @Nullable EquippedItem existing, @NotNull EquipmentSlot slot, int slotIndex, int customInventoryId) {

        // Current item is non-existent
        if (existing == null) {

            // null->null (no update required)
            if (UtilityMethods.isAir(newItem)) return null;

            // null->some
            return new ItemUpdate(slot, null, new EquippedItem(customInventoryId, slot, slotIndex, NBTItem.get(newItem)));
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
        return new ItemUpdate(slot, existing, new EquippedItem(customInventoryId, slot, slotIndex, NBTItem.get(newItem)));
    }

    @NotNull
    public static Optional<ItemStack> optionalOf(@Nullable ItemStack stack) {
        return Optional.of(stack == null ? new ItemStack(Material.AIR) : stack);
    }

    public static void callbackIfNotNull(@Nullable ItemUpdate update, @NotNull Consumer<ItemUpdate> callback) {
        if (update != null) callback.accept(update);
    }
}
