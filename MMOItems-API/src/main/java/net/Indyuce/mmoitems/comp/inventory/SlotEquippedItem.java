package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlotEquippedItem extends EquippedItem {
    private final Player player;
    private final int slotNumber;

    public SlotEquippedItem(@NotNull Player player, int slotNumber, ItemStack item, EquipmentSlot slot) {
        super(item, slot);

        this.player = player;
        this.slotNumber = slotNumber;
    }

    public SlotEquippedItem(@NotNull Player player, int slotNumber, NBTItem item, EquipmentSlot slot) {
        super(item, slot);

        this.player = player;
        this.slotNumber = slotNumber;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public int getSlotNumber() {
        return slotNumber;
    }

    @Override
    public void setItem(@Nullable ItemStack item) {

        switch (getSlotNumber()) {
            case -106:
                getPlayer().getInventory().setItemInOffHand(item);
                break;
            case -7:
                getPlayer().getInventory().setItemInMainHand(item);
                break;
            case 103:
                getPlayer().getInventory().setHelmet(item);
                break;
            case 102:
                getPlayer().getInventory().setChestplate(item);
                break;
            case 101:
                getPlayer().getInventory().setLeggings(item);
                break;
            case 100:
                getPlayer().getInventory().setBoots(item);
                break;
            default:
                getPlayer().getInventory().setItem(getSlotNumber(), item);
                break;
        }
    }
}
