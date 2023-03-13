package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.block.WorldGenTemplate;
import net.Indyuce.mmoitems.util.Pair;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class WorldGenManager implements Listener, Reloadable {
    private final Map<String, WorldGenTemplate> templates = new HashMap<>();

    /*
     * maps a custom block to the world generator template so that it is later
     * easier to access all the blocks which must be placed when generating a
     * world.
     */
    private final Map<CustomBlock, WorldGenTemplate> assigned = new HashMap<>();
    private final Queue<Pair<Location, CustomBlock>> modificationsQueue = new ConcurrentLinkedQueue<>();

    private static final BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP};
    private static final Random random = new Random();

    public WorldGenManager() {
        /*
         * load the worldGenManager even if world gen is not enabled so that if
         * admins temporarily disable it, there is no console error spam saying
         * MI could not find corresponding gen template in config
         */
        reload();

        if (MMOItems.plugin.getLanguage().worldGenEnabled)
            Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
    }

    public WorldGenTemplate getOrThrow(String id) {
        Validate.isTrue(templates.containsKey(id), "Could not find gen template with ID '" + id + "'");

        return templates.get(id);
    }

    /*
     * it is mandatory to call this function after registering the custom block
     * if you want the custom block to be spawning in the worlds
     */
    public void assign(CustomBlock block, WorldGenTemplate template) {
        Validate.notNull(template, "Cannot assign a null template to a custom block");

        assigned.put(block, template);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.isNewChunk())
            return;

        if (e.isAsynchronous())
            generate(e);
        else
            Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> generate(e));
    }

    private void generate(ChunkLoadEvent e) {
        assigned.entrySet()
                .stream()
                .filter(entry -> entry.getValue().canGenerateInWorld(e.getWorld()))
                .forEach(entry -> {
                    final CustomBlock block = entry.getKey();
                    final WorldGenTemplate template = entry.getValue();
                    if (random.nextDouble() > template.getChunkChance())
                        return;

                    for (int i = 0; i < template.getVeinCount(); i++) {
                        int y = random.nextInt(template.getMaxDepth() - template.getMinDepth() + 1) + template.getMinDepth();
                        Location generatePoint = e.getChunk().getBlock(random.nextInt(16), y, random.nextInt(16)).getLocation();

                        if (!template.canGenerate(generatePoint) || generatePoint.getWorld() == null)
                            continue;
                        Block modify = generatePoint.getWorld().getBlockAt(generatePoint);

                        for (int j = 0; j < template.getVeinSize(); j++) {
                            if (template.canReplace(modify.getType()))
                                this.modificationsQueue.add(Pair.of(modify.getLocation(), block));
                            BlockFace nextFace = faces[random.nextInt(faces.length)];
                            modify = modify.getRelative(nextFace);
                        }
                    }
                });


        new BukkitRunnable() {
            @Override
            public void run() {
                if (modificationsQueue.isEmpty()) {
                    this.cancel();
                    return;
                }
                Pair<Location, CustomBlock> pair = modificationsQueue.poll();

                if (Bukkit.isPrimaryThread())
                    setBlockData(pair.getKey().getBlock(), pair.getValue());
                else
                    Bukkit.getScheduler().runTask(MMOItems.plugin, () -> setBlockData(pair.getKey().getBlock(), pair.getValue()));
            }
        }.runTaskTimer(MMOItems.plugin, 0, 5);
    }


    private void setBlockData(Block fModify, CustomBlock block) {
        fModify.setType(block.getState().getType(), false);
        fModify.setBlockData(block.getState().getBlockData(), false);
    }

    public void reload() {
        assigned.clear();
        templates.clear();

        FileConfiguration config = new ConfigFile("gen-templates").getConfig();
        for (String key : config.getKeys(false)) {
            try {
                WorldGenTemplate template = new WorldGenTemplate(config.getConfigurationSection(key));
                templates.put(template.getId(), template);
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "An error occurred when loading gen template '" + key + "': " + exception.getMessage());
            }
        }
    }
}
