package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.CustomBlock;
import net.Indyuce.mmoitems.api.worldgen.WorldGenTemplate;

public class WorldGenManager implements Listener {
	Map<String, WorldGenTemplate> templates = new HashMap<String, WorldGenTemplate>();
	Map<Integer, String> assigned = new HashMap<Integer, String>();
	BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP };
	Random rnd = new Random();
	
	public WorldGenManager() {
		FileConfiguration config = new ConfigFile("gen-templates").getConfig();
		
		config.getKeys(false).forEach(e -> {
			MMOItems.plugin.getLogger().info("WorldGenTemplate: " + e);
			templates.put(e, new WorldGenTemplate(config.getConfigurationSection(e)));
		});
		
		if(MMOItems.plugin.getLanguage().worldGenEnabled) Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}
	
	@EventHandler
	public void a(ChunkLoadEvent event) {
		if(!MMOItems.plugin.getLanguage().worldGenWhitelist.contains(event.getWorld().getName())) return;
		if(!event.isNewChunk()) return;
		
		for(int blocks : assigned.keySet()) {
			WorldGenTemplate wgt = templates.get(assigned.get(blocks));

			if(rnd.nextDouble() < wgt.chunkChance) {
				for(int i = 0; i < wgt.veinCount; i++) {
					int y = rnd.nextInt((wgt.maxDepth - wgt.minDepth) + 1) + wgt.minDepth;
					Location generatePoint = event.getChunk().getBlock(rnd.nextInt(16), y, rnd.nextInt(16)).getLocation();
					if(wgt.canGenerate(generatePoint)) {
						CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(blocks);
						Block modify = event.getWorld().getBlockAt(generatePoint);

						for(int j = 0; j < wgt.veinSize; j++) {
							if(wgt.canReplace(modify.getType())) {
								modify.setType(block.getType(), false);
								modify.setBlockData(block.getBlockData(), false);
							}

							BlockFace nextFace = faces[rnd.nextInt(faces.length)];
							modify = modify.getRelative(nextFace);
						}
					}
				}
			}
		}
	}
	
	public void register(CustomBlock block) {
		assigned.put(block.getId(), block.getTemplateName());
	}
}
