package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    private final Location lastLocation;

    private final UUID playerId;

    /**
     * Used to store which items must be given back to which player
     */
    private static final Map<UUID, DeathItemsHandler> PLAYER_INFO = new WeakHashMap<>();

    /**
     * Instanced when a player dies if some Soulbound items must be kept in the
     * player's inventory and need to be cached before the player respawns.
     * <p>
     * If the player leaves the server leaving one object of this type in server
     * RAM, the cached items need to be dropped if the server closes before the
     * player respawns again.
     */
    public DeathItemsHandler(@NotNull Player player) {
        this.playerId = player.getUniqueId();
        this.lastLocation = player.getLocation();
    }

    public void registerItem(@NotNull ItemStack item) {
        items.add(item);
    }

    public void registerIfNecessary() {
        if (!items.isEmpty()) PLAYER_INFO.put(playerId, this);
    }

    /**
     * Drops items on the ground. Called when the player
     * has not respawned yet and items need to be disposed of!
     * TODO save it inside of configuration files and stop using this method. Give items back when joining
     */
    public void dropItems() {
        for (ItemStack drop : items)
            lastLocation.getWorld().dropItem(lastLocation, drop);
    }

    public void giveItems(@NotNull Player player) {
        final ItemStack[] toArray = this.items.toArray(new ItemStack[0]);
        for (ItemStack drop : player.getInventory().addItem(toArray).values())
            player.getWorld().dropItem(player.getLocation(), drop);
    }

    /**
     * Tries to add the items to the player's inventory, or drops them
     * on the ground if the player's inventory is full. In the meantime,
     * the player's item info is removed from the map.
     *
     * @param player Target player respawning
     */
    public static void readAndRemove(@NotNull Player player) {
        final @Nullable DeathItemsHandler handler = PLAYER_INFO.remove(player.getUniqueId());
        if (handler != null) Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> handler.giveItems(player), 10);
    }

    /**
     * @return Soulbound info of players who have not clicked the respawn button
     * and yet have items cached in server RAM
     */
    @NotNull
    public static Collection<DeathItemsHandler> getActive() {
        return PLAYER_INFO.values();
    }
}
