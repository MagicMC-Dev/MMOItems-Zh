package net.Indyuce.mmoitems.listener;

import com.google.common.collect.Maps;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;

/**
 * mmoitems
 * 19/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BiomeChangeListener implements Listener {

    private final Map<UUID, Biome> biomeMap = Maps.newHashMap();

    /**
     * This listener goal is to update the player inventory when he changes biome.
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled() || !PlayerData.has(e.getPlayer()) || (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()))
            return;
        final UUID uuid = e.getPlayer().getUniqueId();
        final Biome biome = e.getTo().getBlock().getBiome();
        final Biome lastBiome = biomeMap.computeIfAbsent(uuid, u1 -> biome);
        if (biome != lastBiome)
            PlayerData.get(e.getPlayer()).getInventory().scheduleUpdate();
        biomeMap.put(uuid, biome);
    }
}
