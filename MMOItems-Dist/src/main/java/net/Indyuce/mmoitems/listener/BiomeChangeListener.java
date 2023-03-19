package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * mmoitems
 * 19/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BiomeChangeListener implements Listener {


    /**
     * This listener goal is to update the player inventory when he changes biome.
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled() || !PlayerData.has(e.getPlayer()) || (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()))
            return;
        final Biome lastBiome = e.getFrom().getBlock().getBiome();
        final Biome biome = e.getTo().getBlock().getBiome();
        if (biome != lastBiome)
            PlayerData.get(e.getPlayer()).getInventory().scheduleUpdate();
    }
}
