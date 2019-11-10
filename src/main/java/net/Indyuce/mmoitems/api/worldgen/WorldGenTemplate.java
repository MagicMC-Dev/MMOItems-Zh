package net.Indyuce.mmoitems.api.worldgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

public class WorldGenTemplate {
	public double chunkChance;
	public int minDepth, maxDepth, veinSize, veinCount;

	private List<Material> replaceableMaterials = new ArrayList<>();
	private List<String> worldWhitelist = new ArrayList<>(), worldBlacklist = new ArrayList<>();
	private List<String> biomeWhitelist = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
	private boolean slimeChunk;

	public WorldGenTemplate(ConfigurationSection config) {
		List<String> matList = config.getStringList("replace");
		for(String material : matList) {
			Validate.notNull(Material.valueOf(material), "Could not load material: " + material + " from " + config.getName());
			replaceableMaterials.add(Material.valueOf(material));
		}
		
		List<String> worldList = config.getStringList("worlds");
		List<String> biomeList = config.getStringList("biomes");

		for(String world : worldList) {
			if(world.contains("!")) worldBlacklist.add(world.toUpperCase());
			else worldWhitelist.add(world.toUpperCase());
		}
		for(String biome : biomeList) {
			if(biome.contains("!")) biomeBlacklist.add(biome.toUpperCase());
			else biomeWhitelist.add(biome.toUpperCase());
		}
		
		chunkChance = config.getDouble("chunk-chance");
		slimeChunk = config.getBoolean("slime-chunk", false);
		
		String[] depth = config.getString("depth").split("\\=");
		minDepth = Integer.parseInt(depth[0]);
		maxDepth = Integer.parseInt(depth[1]);
		
		veinSize = config.getInt("vein-size");
		veinCount = config.getInt("vein-count");
	}

	public boolean canGenerate(Location pos) {
		if(!worldWhitelist.isEmpty() && !worldWhitelist.contains(pos.getWorld().getName().toUpperCase())) return false;
		if(!worldBlacklist.isEmpty() && worldBlacklist.contains(pos.getWorld().getName().toUpperCase())) return false;
		Biome biome = pos.getWorld().getBiome(pos.getBlockX(), pos.getBlockZ());
		if(!biomeWhitelist.isEmpty() && !biomeWhitelist.contains(biome.name())) return false;
		if(!biomeBlacklist.isEmpty() && biomeBlacklist.contains(biome.name())) return false;
		if(slimeChunk && !pos.getChunk().isSlimeChunk()) return false;

		return true;
	}

	public boolean canReplace(Material type) {
		return replaceableMaterials.isEmpty() || replaceableMaterials.contains(type);
	}
}
