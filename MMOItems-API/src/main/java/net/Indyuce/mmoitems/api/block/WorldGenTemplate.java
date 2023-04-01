package net.Indyuce.mmoitems.api.block;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldGenTemplate {
    private final String id;
    private final double chunkChance;
    private final int minDepth, maxDepth, veinSize, veinCount;

    private final List<Material> replaceable = new ArrayList<>();
    private final List<Material> bordering = new ArrayList<>();
    private final List<Material> notBordering = new ArrayList<>();
    private final List<String> worldWhitelist = new ArrayList<>(), worldBlacklist = new ArrayList<>();
    private final List<String> biomeWhitelist = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
    private final boolean slimeChunk;

    public WorldGenTemplate(ConfigurationSection config) {
        Validate.notNull(config, "Could not read gen template config");

        id = config.getName().toLowerCase().replace(" ", "-").replace("_", "-");
        config.getStringList("replace").forEach(str -> replaceable.add(Material.valueOf(str.toUpperCase().replace("-", "_").replace(" ", "_"))));
        config.getStringList("bordering").forEach(str -> bordering.add(Material.valueOf(str.toUpperCase().replace("-", "_").replace(" ", "_"))));
        config.getStringList("not-bordering").forEach(str -> notBordering.add(Material.valueOf(str.toUpperCase().replace("-", "_").replace(" ", "_"))));

        for (String world : config.getStringList("worlds")) {
            (world.startsWith("!") ? worldBlacklist : worldWhitelist).add(world.toLowerCase().replace("_", "-"));
        }
        for (String biome : config.getStringList("biomes")) {
            (biome.startsWith("!") ? biomeBlacklist : biomeWhitelist).add(biome.toUpperCase().replace("-", "_").replace(" ", "_"));
        }
        chunkChance = config.getDouble("chunk-chance");
        slimeChunk = config.getBoolean("slime-chunk", false);

        String[] depth = config.getString("depth").split("=");
        minDepth = Integer.parseInt(depth[0]);
        maxDepth = Integer.parseInt(depth[1]);

        //Validate.isTrue(minDepth >= 0, "Min depth must be greater than 0");
        //Validate.isTrue(maxDepth < 256, "Max depth must be at most 255");

        veinSize = config.getInt("vein-size");
        veinCount = config.getInt("vein-count");

        Validate.isTrue(veinSize > 0 && veinCount > 0, "Vein size and count must be at least 1");
    }

    public String getId() {
        return id;
    }

    public double getChunkChance() {
        return chunkChance;
    }

    public int getVeinSize() {
        return veinSize;
    }

    public int getVeinCount() {
        return veinCount;
    }

    public int getMinDepth() {
        return minDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public boolean canGenerateInWorld(String worldName) {
        // check world list
        String world = worldName.toLowerCase().replace("_", "-");
        if (!worldWhitelist.isEmpty() && !worldWhitelist.contains(world))
            return false;
        return worldBlacklist.isEmpty() || !worldBlacklist.contains(world);
    }

    public boolean canGenerate(Location pos) {

        // check biome list
        Biome biome = pos.getWorld().getBiome(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        if ((!biomeWhitelist.isEmpty() && !biomeWhitelist.contains(biome.name()))
                || (!biomeBlacklist.isEmpty() && biomeBlacklist.contains(biome.name())))
            return false;

        // check extra options
        if (slimeChunk && !pos.getChunk().isSlimeChunk())
            return false;

        if (!bordering.isEmpty()) {
            if (!checkIfBorderingBlocks(pos))
                return false;
        }

        if (!notBordering.isEmpty())
            return checkIfNotBorderingBlocks(pos);

        // can generate if no restrictions applied
        return true;
    }

    public boolean canReplace(Material type) {
        return replaceable.isEmpty() || replaceable.contains(type);
    }

    public boolean canBorder(Material type) {
        return bordering.isEmpty() || bordering.contains(type);
    }

    public boolean checkIfBorderingBlocks(Location pos) {
        return Arrays.stream(BlockFace.values())
                .map(pos.getBlock()::getRelative)
                .map(Block::getType)
                .allMatch(this::canBorder);
    }

    public boolean canNotBorder(Material type) {
        return !notBordering.isEmpty() && notBordering.contains(type);
    }

    public boolean checkIfNotBorderingBlocks(Location pos) {
        return Arrays.stream(BlockFace.values())
                .map(pos.getBlock()::getRelative)
                .map(Block::getType)
                .allMatch(this::canNotBorder);
    }
}
