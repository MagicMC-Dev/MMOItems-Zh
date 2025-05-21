package net.Indyuce.mmoitems.inventory;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Previously, only one Player Inventory was allowed.
 * This makes it so plugins may register all the Player Inventories they want.
 * <p></p>
 * For context, a 'Player Inventory' tells MMOItems where to look for equipped items,
 * (items that will add their stats to the player).
 */
public class PlayerInventoryManager {

    /**
     * Gets the registered Player Inventories --- The places where MMOItems determines player equipped items.
     */
    @NotNull
    private final List<InventorySupplier> suppliers = new ArrayList<>();

    public void register(@NotNull InventorySupplier supplier) {
        Validate.notNull(supplier, "Supplier cannot be null");

        suppliers.add(supplier);

        if (supplier instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) supplier, MMOItems.plugin);
    }

    public void unregisterIf(@NotNull Predicate<InventorySupplier> filter) {
        suppliers.removeIf(supplier -> {
            if (!filter.test(supplier)) return false;

            if (supplier instanceof Listener) HandlerList.unregisterAll((Listener) supplier);
            return true;
        });
    }

    /**
     * Can be used by external plugins to clear current inventory
     * handlers if you want offhand and mainhand items removed
     * from the player inventory
     */
    public void unregisterAll() {

        // Close all
        for (InventorySupplier supplier : suppliers)
            if (supplier instanceof Listener) HandlerList.unregisterAll((Listener) supplier);

        suppliers.clear();
    }

    @NotNull
    public List<InventorySupplier> getAll() {
        return suppliers;
    }

    /**
     * @return Gets the totality of items from all the PlayerInventories
     *         ie all the items that will add their stats to the player.
     */
    @NotNull
    public List<InventoryWatcher> getWatchers(@NotNull InventoryResolver resolver) {
        return suppliers.stream().map(supplier -> supplier.supply(resolver)).toList();
    }
}
