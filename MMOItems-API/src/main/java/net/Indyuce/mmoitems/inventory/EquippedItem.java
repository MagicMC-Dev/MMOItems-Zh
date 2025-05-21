package net.Indyuce.mmoitems.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.Lazy;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EquippedItem extends io.lumine.mythic.lib.player.inventory.EquippedItem {
    private final NBTItem item;
    private final int itemHash;

    private final Type itemType;
    private final ModifierSource source; // Precomputed value

    // TODO MI7.1 change it to ItemReader. Don't mind performance atm
    @Nullable
    private VolatileMMOItem reader;
    private final Lazy<ItemSet> set;

    private Boolean usable, placementLegal;

    public boolean applied;

    private final List<PlayerModifier> modifiers = new ArrayList<>();

    public EquippedItem(int watcherId, EquipmentSlot slot, int slotId, NBTItem item) {
        super(slot, slotId, watcherId);

        this.item = item;
        this.itemType = Type.get(item);
        this.source = itemType == null ? ModifierSource.OTHER : itemType.getModifierSource();
        this.itemHash = item.getItem().hashCode();
        this.set = Lazy.of(() -> {
            final String tag = item.getString("MMOITEMS_ITEM_SET");
            return MMOItems.plugin.getSets().get(tag);
        });
    }

    @NotNull
    public NBTItem getItem() {
        return item;
    }

    @Nullable
    public Type getItemType() {
        return itemType;
    }

    @Nullable
    public ItemSet getSet() {
        return set.get();
    }

    @NotNull
    @Override
    public ModifierSource getModifierSource() {
        return source;
    }

    @NotNull
    @Override
    public List<PlayerModifier> getModifierCache() {
        return modifiers;
    }

    public int getItemHash() {
        return itemHash;
    }

    public VolatileMMOItem reader() {
        if (reader == null) return reader = new VolatileMMOItem(item);
        return reader;
    }

    public void flushCache() {
        usable = null;
        placementLegal = null;
    }

    public boolean isUsable(RPGPlayer user) {
        if (usable != null) return usable;

        // TODO wtf does `allowDynamic` do?
        return usable = user.canUse(item, false, false);
    }

    /**
     * This is a small optimization which reduces the amount of items
     * taken into account by the MMOItems player inventory handler.
     *
     * @return If item placement is legal
     */
    public boolean isPlacementLegal() {
        if (placementLegal != null) return placementLegal;

        // Vanilla items are ignored
        // TODO MI7 optimize. (Type, ID) are checked too many times
        final @Nullable String typeFormat = item.getString("MMOITEMS_ITEM_TYPE");
        if (typeFormat == null) return placementLegal = false;
        final @Nullable Type type = MMOItems.plugin.getTypes().get(typeFormat);
        if (type == null) return placementLegal = false;

        final ModifierSource modSource = type.getModifierSource();
        // EquipmentSlot.OFF_HAND.isCompatible(modSource, slot) ||
        return placementLegal = EquipmentSlot.MAIN_HAND.isCompatible(modSource, getEquipmentSlot());
    }

    public void setItem(ItemStack item) {
        throw new RuntimeException("TODO");
    }

    @Override
    public String toString() {
        return "EquippedItem{" +
                "slot=" + getEquipmentSlot() +
                ", slotId=" + getSlotIndex() +
                ", watcherId=" + getWatcherId() +
                ", itemHash=" + itemHash +
                ", item=" + item.getItem().getType().name() +
                '}';
    }
}
