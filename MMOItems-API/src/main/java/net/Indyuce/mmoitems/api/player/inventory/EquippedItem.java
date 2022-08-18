package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class EquippedItem {
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
     * The slot this equipped item is defined to be, will this <code>Type</code>
     * actually add register its modifiers to the player when held here?
     * <p>
     * There's a difference between registering modifiers and applying it stats.
     * For instance, modifiers from both hands are registered if the placement is
     * legal but might not be taken into account during stat calculation!
     * <p>
     * An <code>OFF_CATALYST</code> may only add in the <code>OFFHAND</code>, and such.
     */
    public boolean isPlacementLegal() {

        // Find item type
        final String typeFormat = item.getString("MMOITEMS_ITEM_TYPE");
        final Type type = typeFormat == null ? null : Type.get(typeFormat);

        // Vanilla items are ignored
        if (type == null)
            return false;

        // Equips anywhere
        if (slot == EquipmentSlot.OTHER || type.getEquipmentType() == EquipmentSlot.OTHER)
            return true;

        // Hand items
        if (type.isHandItem())
            return slot.isHand();

        return slot == type.getEquipmentType();
    }
}