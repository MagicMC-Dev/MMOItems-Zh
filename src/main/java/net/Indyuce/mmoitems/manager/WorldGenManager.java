package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.block.WorldGenTemplate;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class WorldGenManager implements Listener, Reloadable {
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
	public void a(ChunkLoadEvent event) {
		if(event.isNewChunk()) {
		    Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> assigned.forEach((block, template) -> {
		        if(!template.canGenerateInWorld(event.getWorld())) {
		            return;
		        }
				if(random.nextDouble() < template.getChunkChance())
					for(int i = 0; i < template.getVeinCount(); i++) {
						int y = random.nextInt(template.getMaxDepth() - template.getMinDepth() + 1) + template.getMinDepth();
						Location generatePoint = event.getChunk().getBlock(random.nextInt(16), y, random.nextInt(16)).getLocation();

						if(template.canGenerate(generatePoint)) {
							Block modify = generatePoint.getWorld().getBlockAt(generatePoint);

							for(int j = 0; j < template.getVeinSize(); j++) {
								if(template.canReplace(modify.getType())) {
									final Block fModify = modify;
									Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {
										fModify.setType(block.getState().getType(), false);
										fModify.setBlockData(block.getState().getBlockData(), false);
									});
								}

								BlockFace nextFace = faces[random.nextInt(faces.length)];
								modify = modify.getRelative(nextFace);
							}
						}
					}
			}));
		}
	}

	public void reload() {
		assigned.clear();
		templates.clear();

		FileConfiguration config = new ConfigFile("gen-templates").getConfig();
		for(String key : config.getKeys(false)) {
			try {
				WorldGenTemplate template = new WorldGenTemplate(config.getConfigurationSection(key));
				templates.put(template.getId(), template);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "An error occured when loading gen template '" + key + "': " + exception.getMessage());
			}
		}
	}
}
