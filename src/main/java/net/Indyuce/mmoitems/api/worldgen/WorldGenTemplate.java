package net.Indyuce.mmoitems.api.worldgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

public class WorldGenTemplate {
	public final double chunkChance;
	public final int minDepth, maxDepth, veinSize, veinCount;

	private final List<Material> replaceable = new ArrayList<>();
	private final List<String> worldWhitelist = new ArrayList<>(), worldBlacklist = new ArrayList<>();
	private final List<String> biomeWhitelist = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
	private final boolean slimeChunk;

	public WorldGenTemplate(ConfigurationSection config) {
		Validate.notNull(config, "Could not read gen template config");

		config.getStringList("replace").forEach(str -> replaceable.add(Material.valueOf(str.toUpperCase().replace("-", "_").replace(" ", "_"))));

		for (String world : config.getStringList("worlds"))
			if (world.contains("!"))
				worldBlacklist.add(world.toUpperCase());
			else
				worldWhitelist.add(world.toUpperCase());

		for (String biome : config.getStringList("biomes"))
			if (biome.contains("!"))
				biomeBlacklist.add(biome.toUpperCase());
			else
				biomeWhitelist.add(biome.toUpperCase());
		
		chunkChance = config.getDouble("chunk-chance");
		slimeChunk = config.getBoolean("slime-chunk", false);

		String[] depth = config.getString("depth").split("\\=");
		minDepth = Integer.parseInt(depth[0]);
		maxDepth = Integer.parseInt(depth[1]);

		veinSize = config.getInt("vein-size");
		veinCount = config.getInt("vein-count");
	}

	public boolean canGenerate(Location pos) {

		// check world list
		if (!worldWhitelist.isEmpty() && !worldWhitelist.contains(pos.getWorld().getName().toUpperCase()))
			return false;
		if (!worldBlacklist.isEmpty() && worldBlacklist.contains(pos.getWorld().getName().toUpperCase()))
			return false;

		// check biome list
		Biome biome = pos.getWorld().getBiome(pos.getBlockX(), pos.getBlockZ());
		if (!biomeWhitelist.isEmpty() && !biomeWhitelist.contains(biome.name()))
			return false;
		if (!biomeBlacklist.isEmpty() && biomeBlacklist.contains(biome.name()))
			return false;

		// check extra options
		if (slimeChunk && !pos.getChunk().isSlimeChunk())
			return false;

		// can generate if no restrictions applied
		return true;
	}

	public boolean canReplace(Material type) {
		return replaceable.isEmpty() || replaceable.contains(type);
	}
}
