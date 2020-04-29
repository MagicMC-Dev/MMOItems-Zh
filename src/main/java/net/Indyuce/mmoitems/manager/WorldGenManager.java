package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
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
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.block.WorldGenTemplate;

public class WorldGenManager implements Listener {
	private final Map<String, WorldGenTemplate> templates = new HashMap<>();

	/*
	 * maps a custom block to the world generator template so that it is later
	 * easier to access all the blocks which must be placed when generating a
	 * world.
	 */
	private final Map<CustomBlock, WorldGenTemplate> assigned = new HashMap<>();

	private static final BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP };
	private static final Random random = new Random();

	public WorldGenManager() {
		if (!MMOItems.plugin.getLanguage().worldGenEnabled)
			return;

		reload();
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	public WorldGenTemplate getOrThrow(String id) {
		Validate.isTrue(templates.containsKey(id), "Could not find gen template with ID '" + id + "'");
		return templates.get(id);
	}

	/*
	 * it is mandatory to call this function after registering the custom block
	 * if you want the custom block to be
	 */
	public void assign(CustomBlock block, WorldGenTemplate template) {
		Validate.notNull(template, "Cannot assign a null template to a custom block");
		assigned.put(block, template);
	}

	@EventHandler
	public void a(ChunkLoadEvent event) {
		if (event.isNewChunk())
			assigned.forEach((block, template) -> {
				if (random.nextDouble() < template.getChunkChance())
					for (int i = 0; i < template.getVeinCount(); i++) {
						int y = random.nextInt(template.getMaxDepth() - template.getMinDepth() + 1) + template.getMinDepth();
						Location generatePoint = event.getChunk().getBlock(random.nextInt(16), y, random.nextInt(16)).getLocation();

						if (template.canGenerate(generatePoint)) {
							Block modify = event.getWorld().getBlockAt(generatePoint);

							for (int j = 0; j < template.getVeinSize(); j++) {
								if (template.canReplace(modify.getType())) {
									modify.setType(block.getState().getType(), false);
									modify.setBlockData(block.getState().getBlockData(), false);
								}

								BlockFace nextFace = faces[random.nextInt(faces.length)];
								modify = modify.getRelative(nextFace);
							}
						}
					}
			});
	}

	public void reload() {
		templates.clear();

		FileConfiguration config = new ConfigFile("gen-templates").getConfig();
		for (String key : config.getKeys(false))
			try {
				WorldGenTemplate template = new WorldGenTemplate(config.getConfigurationSection(key));
				templates.put(template.getId(), template);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "An error occured when loading gen template '" + key + "': " + exception.getMessage());
			}
	}
}
