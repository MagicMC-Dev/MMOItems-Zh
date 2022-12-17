package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.api.InventoryAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells MMOItems where to find additional equipment.
 * <p></p>
 * RPGInventory stuff - Passive Items
 */
public class RPGInventoryHook implements PlayerInventory, Listener {

    @Override
    public List<EquippedItem> getInventory(Player player) {
        List<EquippedItem> list = new ArrayList<>();

        for (ItemStack passive : InventoryAPI.getPassiveItems(player))
            if (passive != null)
                list.add(new LegacyEquippedItem(passive));

        return list;
    }

    @EventHandler
    public void a(InventoryCloseEvent event) {
        if (InventoryAPI.isRPGInventory(event.getInventory()))
            PlayerData.get((Player) event.getPlayer()).updateInventory();
    }

    public class LegacyEquippedItem extends EquippedItem {
        public LegacyEquippedItem(ItemStack item) {
            super(item, EquipmentSlot.ACCESSORY);
        }

        @Override
        public void setItem(@Nullable ItemStack item) {
            final ItemStack ref = getNBT().getItem();
            ref.setType(item.getType());
            ref.setAmount(item.getAmount());
            ref.setItemMeta(ref.getItemMeta());
        }
    }
}
