package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.api.world.MMOBlockPopulator;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.Map;

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

//    @EventHandler
//    public void onWorldInit(WorldInitEvent e) {
//        MMOItems.log("Initializing world " + e.getWorld().getName());
//        final World world = e.getWorld();
//        world.getPopulators().add(manager.populator(world));
//    }

    private final Map<String, MMOBlockPopulator> populatorMap = new HashMap<>();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        final World world = e.getWorld();
        if (!e.isNewChunk() || populatorMap.containsKey(world.getName())) return;
        MMOBlockPopulator populator = manager.populator(world);
        world.getPopulators().add(populator);
        populatorMap.put(world.getName(), populator);
    }
}
