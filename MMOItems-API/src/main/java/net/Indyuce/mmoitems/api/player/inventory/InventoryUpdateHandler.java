package net.Indyuce.mmoitems.api.player.inventory;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * <p>
 * It's one of the most urgent systems to update. Moving everything to a new
 * class to mark everything that needs to be changed
 *
 * @author indyuce
 */
public class InventoryUpdateHandler {
    private final PlayerData player;

    private final List<EquippedItem> items = new ArrayList<>();

    @Deprecated
    public ItemStack helmet = null, chestplate = null, leggings = null, boots = null, hand = null, offhand = null;

    /**
     * Used to handle player inventory updates.
     */
    public InventoryUpdateHandler(PlayerData player) {
        this.player = player;
    }

    /**
     * This list includes items which satisfy the following conditions
     * - inventory placement is legal (a hand item placed in a hand slot, an armor placed in an armor slot)
     * - the player meets the item requirements (class, level etc)
     *
     * @return All equipped MMOItems in the player's inventory. Also includes
     *         items from custom inventory plugins like MMOInventory
     */
    public List<EquippedItem> getEquipped() {
        return items;
    }

    public void updateCheck() {
        PlayerInventory inv = player.getPlayer().getInventory();
        if (isDifferent(helmet, inv.getHelmet()) || isDifferent(chestplate, inv.getChestplate()) || isDifferent(leggings, inv.getLeggings())
                || isDifferent(boots, inv.getBoots()) || isDifferent(hand, inv.getItemInMainHand()) || isDifferent(offhand, inv.getItemInOffHand()))
            player.updateInventory();
    }

    /**
     * Schedules an inventory update in one tick
     */
    public void scheduleUpdate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, player::updateInventory);
    }

    private boolean isDifferent(ItemStack item, ItemStack item1) {
        if (item == null && item1 == null)
            return false;

        if ((item == null && item1 != null) || (item != null && item1 == null))
            return true;

        // Check hash code first to spare calculations
        return item.hashCode() != item1.hashCode() && !item.equals(item1);
    }
}
