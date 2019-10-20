package net.Indyuce.mmoitems.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.CustomBlock;
import net.Indyuce.mmoitems.api.util.MushroomState;

public class BlockManager {
	final static List<Integer> downIds = Arrays.asList(new Integer[] {23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,
		48,49,50,51,52,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,130,131,132,133,134,135,
		136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160});
	final static List<Integer> eastIds = Arrays.asList(new Integer[] {10,11,12,13,14,15,16,17,18,19,20,21,22,39,40,41,42,43,44,45,46,47,48,49,50,
		51,52,53,59,60,61,62,63,64,65,66,67,68,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,115,116,117,118,119,120,121,122,123,124,125,126,
		127,128,129,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160});
	final static List<Integer> northIds = Arrays.asList(new Integer[] {4,5,6,7,8,9,16,17,18,19,20,21,22,31,32,33,34,35,36,37,38,47,48,49,50,51,52,
		53,55,56,57,58,63,64,65,66,67,68,77,78,79,80,81,82,83,84,93,94,95,96,97,98,99,107,108,109,110,111,112,113,114,123,124,125,126,127,
		128,129,138,139,140,141,142,143,144,145,154,155,156,157,158,159,160});
	final static List<Integer> southIds = Arrays.asList(new Integer[] {2,3,6,7,8,9,13,14,15,19,20,21,22,27,28,29,30,35,36,37,38,43,44,45,46,51,52,
		53,55,56,57,58,61,62,65,66,67,68,73,74,75,76,81,82,83,84,89,90,91,92,97,98,99,103,104,105,106,111,112,113,114,119,120,121,122,127,
		128,129,134,135,136,137,142,143,144,145,150,151,152,153,158,159,160});
	final static List<Integer> upIds = Arrays.asList(new Integer[] {8,9,12,15,18,21,22,25,26,29,30,33,34,37,38,41,42,45,46,49,50,53,57,58,60,62,
		64,67,68,71,72,75,76,79,80,83,84,87,88,91,92,95,96,99,101,102,105,106,109,110,113,114,117,118,121,122,125,126,128,129,132,133,
		136,137,140,141,144,145,148,149,152,153,156,157,160});
	final static List<Integer> westIds = Arrays.asList(new Integer[] {1,3,5,7,9,11,12,14,15,17,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,
		50,52,56,58,59,60,61,62,63,64,66,68,70,72,74,76,78,80,82,84,86,88,90,92,94,96,98,100,102,104,106,108,110,112,114,116,118,120,122,
		124,126,129,131,133,135,137,139,141,143,145,147,149,151,153,155,157,159});
	
	FileConfiguration config;
	private final Map<Integer, CustomBlock> customBlocks = new HashMap<>();
	private final Map<Integer, Integer> mushroomStateValue = new HashMap<>();

	public BlockManager() {
		config = new ConfigFile("custom-blocks").getConfig();
		
		for(int i = 1; i < 161; i++) {
			if(i != 54) {
				MushroomState state = new MushroomState(getType(i), upIds.contains(i), downIds.contains(i), westIds.contains(i),
					eastIds.contains(i), southIds.contains(i), northIds.contains(i));
				customBlocks.put(i, new CustomBlock(i, state, config.getConfigurationSection("" + i)));
				mushroomStateValue.put(state.getUniqueId(), i);
			}
		}
	}
	
	public Material getType(int id) {
		if(id < 54) return Material.BROWN_MUSHROOM_BLOCK;
		else if(id > 99) return Material.MUSHROOM_STEM;
		else return Material.RED_MUSHROOM_BLOCK;
	}

	public CustomBlock getBlock(int id) {
		if(id > 0 && id < 161 && id != 54)
			return customBlocks.get(id);

		MMOItems.plugin.getLogger().warning("Invalid CustomBlock Id! " + id);
		return null;
	}
	public CustomBlock getBlock(MushroomState state)
	{ return customBlocks.get(mushroomStateValue.get(state.getUniqueId())); }
	
	public void reload() {
		for(CustomBlock block : customBlocks.values())
			block.reload();
	}

	public Collection<CustomBlock> getAll()
	{ return customBlocks.values(); }
	public Set<Integer> getBlockIds()
	{ return customBlocks.keySet(); }

	public FileConfiguration getConfig()
	{ return config; }

	public boolean isVanilla(MushroomState state) {
		return !mushroomStateValue.containsKey(state.getUniqueId());
	}
	
	public boolean isMushroomBlock(Material type) {
		return (type == Material.BROWN_MUSHROOM_BLOCK || type == Material.MUSHROOM_STEM || type == Material.RED_MUSHROOM_BLOCK);
	}
}
