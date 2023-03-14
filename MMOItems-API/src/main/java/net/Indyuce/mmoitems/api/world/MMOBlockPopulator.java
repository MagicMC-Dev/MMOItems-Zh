package net.Indyuce.mmoitems.api.world;

import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.block.WorldGenTemplate;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

/**
 * mmoitems
 * 14/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MMOBlockPopulator extends BlockPopulator {

    private static final BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP};

    private final WorldGenManager manager;
    private final World world;

    public MMOBlockPopulator(World world, WorldGenManager manager) {
        this.manager = manager;
        this.world = world;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        final Map<CustomBlock, WorldGenTemplate> assigned = manager.assigned();
        assigned.entrySet()
                .stream()
                .filter(entry -> entry.getValue().canGenerateInWorld(worldInfo.getName()))
                .filter(entry -> entry.getValue().getMinDepth() >= worldInfo.getMinHeight())
                .filter(entry -> entry.getValue().getMaxDepth() <= worldInfo.getMaxHeight())
                .forEach(entry -> {
                    final CustomBlock block = entry.getKey();
                    final WorldGenTemplate template = entry.getValue();
                    if (random.nextDouble() > template.getChunkChance())
                        return;

                    for (int i = 0; i < template.getVeinCount(); i++) {
                        int x = chunkX * 16 + random.nextInt(16);
                        int y = random.nextInt(template.getMaxDepth() - template.getMinDepth() + 1) + template.getMinDepth();
                        int z = chunkZ * 16 + random.nextInt(16);
                        Location generatePoint = new Location(world, x, y, z);

                        if (!template.canGenerate(generatePoint) || generatePoint.getWorld() == null)
                            continue;
                        Block modify = generatePoint.getWorld().getBlockAt(generatePoint);

                        // MMOItems.log("Generating " + block.getId() + " at x: " + generatePoint.getBlockX() + " y: " + generatePoint.getBlockY() + " z: " + generatePoint.getBlockZ());
                        for (int j = 0; j < template.getVeinSize(); j++) {
                            if (!limitedRegion.isInRegion(modify.getLocation()))
                                continue;
                            if (template.canReplace(limitedRegion.getType(modify.getLocation()))) {
                                limitedRegion.setType(modify.getLocation(), block.getState().getType());
                                limitedRegion.setBlockData(modify.getLocation(), block.getState().getBlockData());
                            }
                            BlockFace nextFace = faces[random.nextInt(faces.length)];
                            modify = modify.getRelative(nextFace);
                        }
                    }
                });
    }

}
