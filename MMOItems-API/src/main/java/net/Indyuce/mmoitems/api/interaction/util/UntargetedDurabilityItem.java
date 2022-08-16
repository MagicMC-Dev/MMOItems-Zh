package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UntargetedDurabilityItem extends DurabilityItem {
    private final EquipmentSlot slot;

    /**
     * Allows to handle custom durability for target weapons when
     * they are left/right clicked while using the same durability
     * system for both weapon types
     */
    public UntargetedDurabilityItem(Player player, NBTItem item, EquipmentSlot slot) {
        super(player, item);

        this.slot = slot;
    }

    @Override
    public UntargetedDurabilityItem decreaseDurability(int loss) {
        return (UntargetedDurabilityItem) super.decreaseDurability(loss);
    }

    public void inventoryUpdate() {

        ItemStack newVersion = toItem();
        if (newVersion == null) {
            if (slot == EquipmentSlot.OFF_HAND)
                getPlayer().getInventory().setItemInOffHand(null);
            else
                getPlayer().getInventory().setItemInMainHand(null);
            return;
        }

        getNBTItem().getItem().setItemMeta(toItem().getItemMeta());
    }
}
