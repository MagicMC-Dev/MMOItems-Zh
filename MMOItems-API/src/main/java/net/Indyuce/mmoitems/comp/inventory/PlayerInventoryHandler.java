package net.Indyuce.mmoitems.comp.inventory;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Previously, only one Player Inventory was allowed.
 * This makes it so plugins may register all the Player Inventories they want.
 * <p></p>
 * For context, a 'Player Inventory' tells MMOItems where to look for equipped items,
 * (items that will add their stats to the player).
 */
public class PlayerInventoryHandler {

    /**
     * Gets the registered Player Inventories --- The places where MMOItems determines player equipped items.
     */
    @NotNull
    private final List<PlayerInventory> registeredInventories = new ArrayList<>();

    /**
     * Use this to tell MMOItems to search equipment here ---
     * Items that will give their stats to the player.
     * <p></p>
     * Note that if the items are not held in the correct slot (OFF_CATALYST not held in OFFHAND)
     * they won't provide their stats. You may fool MMOItems by telling the <code>EquippedItem</code> that it is
     * in their correct slot though, but that is up to you.
     * <p></p>
     * <b>Calling twice will cause duplicates</b> but is nevertheless allowed if you really want to.
     */
    public void register(@NotNull PlayerInventory pInventory) {
        registeredInventories.add(pInventory);
        if (pInventory instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) pInventory, MMOItems.plugin);
    }

    public void unregisterIf(Predicate<PlayerInventory> filter) {
        Iterator<PlayerInventory> iterator = registeredInventories.iterator();
        while (iterator.hasNext()) {
            PlayerInventory next = iterator.next();
            if (filter.test(next)) {
                if (next instanceof Listener)
                    HandlerList.unregisterAll((Listener) next);
                iterator.remove();
            }
        }
    }

    /**
     * Can be used by external plugins to clear current inventory
     * handlers if you want offhand and mainhand items removed
     * from the player inventory
     */
    public void unregisterAll() {

        // Unregister events
        for (PlayerInventory inv : registeredInventories)
            if (inv instanceof Listener)
                HandlerList.unregisterAll((Listener) inv);

        registeredInventories.clear();
    }

    /**
     * @return A copy of the list of registered inventories.
     */
    public ArrayList<PlayerInventory> getAll() {
        return new ArrayList<>(registeredInventories);
    }

    /**
     * @return Gets the totality of items from all the PlayerInventories ie all the items that will add their stats to the player.
     */
    @NotNull
    public List<EquippedItem> getInventory(@NotNull Player player) {

        // Get and add lists from every registered inventories
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
