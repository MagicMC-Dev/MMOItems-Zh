package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells MMOItems where to find additional equipment.
 * <p></p>
 * Ornaments - Found in any inventory slot.
 */
public class OrnamentPlayerInventory implements PlayerInventory, Listener {

    @Override
    public List<EquippedItem> getInventory(Player player) {
        final List<EquippedItem> list = new ArrayList<>();

        // Find ornaments
        final ItemStack[] matrix = player.getInventory().getContents();
        for (int i = 0; i < matrix.length; i++) {
            final ItemStack curr = matrix[i];
            if (curr == null || curr.getType() == Material.AIR)
                continue;

            final NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(curr);
            final @Nullable Type itemType = Type.get(nbtItem.getType());
            if (itemType != null && itemType.getSupertype().equals(Type.ORNAMENT))
                list.add(new SlotEquippedItem(player, i, nbtItem, EquipmentSlot.OTHER));
        }

        return list;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void updateOnItemPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            final NBTItem nbt = NBTItem.get(event.getItem().getItemStack());
            if (nbt.hasType() && Type.get(nbt.getType()).getSupertype().equals(Type.ORNAMENT))
                PlayerData.get((Player) event.getEntity()).getInventory().scheduleUpdate();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void updateOnItemDrop(PlayerDropItemEvent event) {
        final NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
        if (nbt.hasType() && Type.get(nbt.getType()).getSupertype().equals(Type.ORNAMENT))
            PlayerData.get(event.getPlayer()).updateInventory();
    }
}
