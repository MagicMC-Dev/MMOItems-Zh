package net.Indyuce.mmoitems.listener.option;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Useful Resources:
 * - <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryAction.html">...</a>
 * - <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html">...</a>
 *
 * @author Jules
 */
public class SoulboundNoDrop implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void cannotDrop(PlayerDropItemEvent event) {
        if (isBound(event.getItemDrop().getItemStack(), event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void cannotDragAround(InventoryDragEvent event) {

        final VInventoryView view = VersionUtils.getView(event);
        if (view.getType() == InventoryType.CRAFTING) return;

        // This easily allows to check if the item was dragged in or out of the player's inventory
        final int topInventorySize = view.getTopInventory().getContents().length;
        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet())
            if (entry.getKey() < topInventorySize && isBound(entry.getValue(), event.getWhoClicked())) {
                event.setCancelled(true);
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void cannotMoveAround(InventoryClickEvent event) {

        // Can only move around in
        if (VersionUtils.getView(event).getType() == InventoryType.CRAFTING) return;

        try {

            // Depends on click and inventory type.
            final boolean result = isSafe(event);
            if (!result) event.setCancelled(true);
        } catch (RuntimeException exception) {

            // Safe check...
            if (isBound(event.getCurrentItem(), event.getWhoClicked()) || isBound(event.getCursor(), event.getWhoClicked()))
                event.setCancelled(true);
        }
    }

    private boolean isSafe(@NotNull InventoryClickEvent event) {
        switch (event.getAction()) {

            /*
             * Pickups
             */
            case NOTHING: // 'Nothing happens' is safe enough
            case PICKUP_ALL: // Can pickup any item
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case COLLECT_TO_CURSOR: // Considered a pickup
            case CLONE_STACK: // (Creative) Clones currentItem into cursor. Considered a pickup
            case HOTBAR_MOVE_AND_READD: // Some is given to the player, but not the target inventory, hence safe!
                return true;

            /*
             * Drop cursor. Check cursor
             */
            case DROP_ONE_CURSOR: // Check cursor (dropped)
            case DROP_ALL_CURSOR:
                return !isBound(event.getCursor(), event.getWhoClicked());

            /*
             * Drop current item. Check current item
             */
            case DROP_ALL_SLOT: // Check current item (dropped)
            case DROP_ONE_SLOT:
                return !isBound(event.getCurrentItem(), event.getWhoClicked());

            /*
             * Places. Check cursor only if place is in remove inventory
             */
            case SWAP_WITH_CURSOR:
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE: {

                // Can place any item in player's inventory
                if (event.getClickedInventory().getType() == InventoryType.PLAYER) return true;

                // Only accepted if the item is not soulbound
                return !isBound(event.getCursor(), event.getWhoClicked());
            }

            /*
             * Swap with hotbar. Check hotbar item only if
             * swap is done with remote inventory
             */
            case HOTBAR_SWAP: {

                // Can place any item in player's inventory
                if (event.getClickedInventory().getType() == InventoryType.PLAYER) return true;

                // Check hotbar
                final ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                return !isBound(hotbarItem, event.getWhoClicked());
            }

            /*
             * Shift click item move. Check current item only if
             * being placed in remove inventory
             */
            case MOVE_TO_OTHER_INVENTORY: {

                // Can move anything to player's inventory
                if (event.getClickedInventory().getType() != InventoryType.PLAYER) return true;

                // Check current item
                return !isBound(event.getCurrentItem(), event.getWhoClicked());
            }

            /*
             * For anything else, check both current item and cursor for safeguard.
             * Maybe caused by 1.20.6+ inventory actions and other plugins.
             */
            case UNKNOWN:
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private boolean isBound(@Nullable ItemStack item, @NotNull HumanEntity player) {
        return item != null && item.hasItemMeta() && MMOUtils.isSoulboundTo(NBTItem.get(item), (Player) player);
    }
}


