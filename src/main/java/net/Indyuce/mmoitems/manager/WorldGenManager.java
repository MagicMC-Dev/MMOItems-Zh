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
	private final Map<String, WorldGenTemplate> templates = new HashMap<>();
	private final Map<Integer, String> assigned = new HashMap<>();

	private static final BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP };
	private static final Random random = new Random();

	public WorldGenManager() {
		if (!MMOItems.plugin.getLanguage().worldGenEnabled)
			return;

		FileConfiguration config = new ConfigFile("gen-templates").getConfig();
		config.getKeys(false).forEach(e -> templates.put(e, new WorldGenTemplate(config.getConfigurationSection(e))));
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	public void register(CustomBlock block) {
		assigned.put(block.getId(), block.getTemplateName());
	}

	@EventHandler
	public void a(ChunkLoadEvent event) {
		if (!event.isNewChunk())
			return;

		for (int blocks : assigned.keySet()) {
			WorldGenTemplate wgt = templates.get(assigned.get(blocks));

			if (random.nextDouble() < wgt.chunkChance)
				for (int i = 0; i < wgt.veinCount; i++) {
					int y = random.nextInt((wgt.maxDepth - wgt.minDepth) + 1) + wgt.minDepth;
					Location generatePoint = event.getChunk().getBlock(random.nextInt(16), y, random.nextInt(16)).getLocation();
					if (wgt.canGenerate(generatePoint)) {
						CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(blocks);
						Block modify = event.getWorld().getBlockAt(generatePoint);

						for (int j = 0; j < wgt.veinSize; j++) {
							if (wgt.canReplace(modify.getType())) {
								modify.setType(block.getType(), false);
								modify.setBlockData(block.getBlockData(), false);
							}

							BlockFace nextFace = faces[random.nextInt(faces.length)];
							modify = modify.getRelative(nextFace);
						}
					}
				}
		}
	}
}
