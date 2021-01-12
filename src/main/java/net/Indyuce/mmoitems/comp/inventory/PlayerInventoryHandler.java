package net.Indyuce.mmoitems.comp.inventory;

import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Previously, only one Player Inventory was allowed.
 * This makes it so plugins may register all the Player Inventories they want.
 * <p></p>
 * For context, a 'Player Inventory' tells MMOItems where to look for equipped items,
 * (items that will add their stats to the player).
 */
public class PlayerInventoryHandler {
    public PlayerInventoryHandler() {}

    /**
     * Gets the registered Player Inventories --- The places where MMOItems determines player equipped items.
     */
    @NotNull ArrayList<PlayerInventory> registeredInventories = new ArrayList<>();

    /**
     * Use this to tell MMOItems to search equipment here ---
     * Items that will give their stats to the player.
     * <p></p>
     * Note that if the items are not held in the correct slot (OFF_CATALYST not held in OFFHAND)
     * they wont provide their stats. You may fool MMOItems by telling the <code>EquippedItem</code> that it is
     * in their correct slot though, but that is up to you.
     * <p></p>
     * <b>Calling twice will cause duplicates</b> but is nevertheless allowed if you really want to.
     */
    public void register(@NotNull PlayerInventory pInventory) {

        // Add
        registeredInventories.add(pInventory);
    }

    /**
     * I guess if your plugin is so custom it doesnt want Armor Slots / mainahnd / offhand to add
     * their stats by default you would use this.
     */
    public void unregisterAll() {

        // Unregister events
        for (PlayerInventory inv : registeredInventories) {

            // Is it a listener?
            if (inv instanceof Listener) {

                // Unregister events
                HandlerList.unregisterAll((Listener) inv);
            }
        }

        // Add
        registeredInventories.clear();
    }

    /**
     * Gets a copy of the list of registered inventories.
     */
    public ArrayList<PlayerInventory> getAll() {

        // Add
        return new ArrayList<>(registeredInventories);
    }

    /**
     * Gets the totality of items from all the PlayerInventories.
     * <p></p>
     * All the items that will add their stats to the player.
     */
    @NotNull public List<EquippedItem> getInventory(@NotNull Player player) {

        // Get and add lists from every registerde inventories
        ArrayList<EquippedItem> cummulative = new ArrayList<>();

        // For every registered inventory
        for (PlayerInventory inv : registeredInventories) {

            // Get
            cummulative.addAll(inv.getInventory(player));
        }

        // Return thay result
        return cummulative;
    }
}
