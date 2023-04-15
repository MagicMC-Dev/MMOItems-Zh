package net.Indyuce.mmoitems.api;

import java.util.*;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathItemsHandler {

    /**
     * Items that need to be given to the player whenever he respawns.
     */
    private final List<ItemStack> items = new ArrayList<>();

    /**
     * If the player leaves the server before removing, the cached items will be
     * lost. the plugin saves the last location of the player to drop the items
     * when the server shuts down this way they are 'saved'
     */
    private final Player player;

    /**
     * Used to store which items must be given back to which player
     */
    private static final Map<UUID, DeathItemsHandler> INFO = new WeakHashMap<>();

    /**
     * Instanced when a player dies if some Soulbound items must be kept in the
     * player's inventory and need to be cached before the player respawns.
     * <p>
     * If the player leaves the server leaving one object of this type in server
     * RAM, the cached items need to be dropped if the server closes before the
     * player respawns again.
     */
    public DeathItemsHandler(@NotNull Player player) {
        this.player = player;
    }

    public void registerItem(@NotNull ItemStack item) {
        items.add(item);
    }

    public void registerIfNecessary() {
        if (!items.isEmpty()) INFO.put(player.getUniqueId(), this);
    }

    /**
     * @param forceDrop Should the items all drop on the ground
     */
    public void giveItems(boolean forceDrop) {

        // Drop all items on the ground
        if (forceDrop) for (ItemStack drop : items)
            player.getWorld().dropItem(player.getLocation(), drop);

            // First try to add them to inventory
        else {
            final ItemStack[] toArray = this.items.toArray(new ItemStack[0]);
            for (ItemStack drop : player.getInventory().addItem(toArray).values())
                player.getWorld().dropItem(player.getLocation(), drop);
        }
    }

    /**
     * Tries to add the items to the player's inventory, or drops them
     * on the ground if the player's inventory is full. In the meantime,
     * the player's item info is removed from the map.
     *
     * @param player Target player respawning
     */
    public static void readAndRemove(@NotNull Player player) {
        final @Nullable DeathItemsHandler handler = INFO.remove(player.getUniqueId());
        if (handler != null) Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> handler.giveItems(false), 10);
    }

    /**
     * @return Soulbound info of players who have not clicked the respawn button
     *         and yet have items cached in server RAM
     */
    public static Collection<DeathItemsHandler> getActive() {
        return INFO.values();
    }
}
