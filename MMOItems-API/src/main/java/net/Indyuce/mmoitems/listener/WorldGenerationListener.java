package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.manager.WorldGenManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * mmoitems
 * 13/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class WorldGenerationListener implements Listener {

    private final WorldGenManager manager;

    public WorldGenerationListener(WorldGenManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!e.isNewChunk()) return;
        manager.populate(e);
    }
}
