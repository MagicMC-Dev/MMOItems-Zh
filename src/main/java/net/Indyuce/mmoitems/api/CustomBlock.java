package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.MushroomState;
import net.Indyuce.mmoitems.manager.BlockManager;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class CustomBlock {
	MushroomState state;
	
	private final int id;
	private String blockName = ChatColor.RESET + "Custom Block";
	private String templateName = "";
	private List<String> lore = new ArrayList<String>();
	private int minXP = 0;
	private int maxXP = 0;
	private int requiredPower = 0;
	
	public CustomBlock(int id, MushroomState state, ConfigurationSection config) {
		this.id = id;
		this.state = state;
		
		if(config != null) {
			if(config.contains("display-name")) blockName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name"));
			if(config.contains("lore"))
				for(String s : config.getStringList("lore"))
					lore.add(ChatColor.translateAlternateColorCodes('&', s));
			if(config.contains("min-xp")) minXP = config.getInt("min-xp");
			if(config.contains("max-xp")) maxXP = config.getInt("max-xp");
			if(config.contains("required-power")) requiredPower = config.getInt("required-power");
			if(config.contains("gen-template")) {
				templateName = config.getString("gen-template");
				
				MMOItems.plugin.getWorldGen().register(this);
			}
		}
	}

	//Depending on the id, return the mushroom type this block
	//is supposed to feature.
	public Material getType() {
		return MMOItems.plugin.getCustomBlocks().getType(id);
	}
	
	//From the Id, check which sides to apply data to.
	//This will return the blockstate of the blocks id.
	public BlockData getBlockData() {
		MultipleFacing mfData = (MultipleFacing) getType().createBlockData();
		
		mfData.setFace(BlockFace.UP, state.up);
		mfData.setFace(BlockFace.DOWN, state.down);
		mfData.setFace(BlockFace.NORTH, state.north);
		mfData.setFace(BlockFace.SOUTH, state.south);
		mfData.setFace(BlockFace.EAST, state.east);
		mfData.setFace(BlockFace.WEST, state.west);
		
		return mfData;
	}
	
	//Convert block data into Item
	public ItemStack getItem() {
		ItemStack item = new ItemStack(Material.CLAY_BALL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(blockName);
		meta.setLore(lore);
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.values());

		if(MMOItems.plugin.getVersion().isBelowOrEqual(1, 13)) ((Damageable) meta).setDamage(id);
		
        item.setItemMeta(meta);

        return MMOItems.plugin.getNMS().getNBTItem(item)
        	.addTag(new ItemTag("MMOITEMS_DISABLE_CRAFTING", true),
        		new ItemTag("MMOITEMS_DISABLE_SMITHING", true),
        		new ItemTag("MMOITEMS_DISABLE_ENCHANTING", true),
        		new ItemTag("MMOITEMS_DISABLE_REPAIRING", true),
        		new ItemTag("MMOITEMS_BLOCK_ID", id),
        		new ItemTag("CustomModelData", id + 1000))
        	.toItem();
	}
	
	public void reload() {
		ConfigurationSection config = MMOItems.plugin.getCustomBlocks().getConfig().getConfigurationSection(id + "");
		lore.clear();
		
		if(config != null) {
			blockName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", "&rCustom Block"));
			for(String s : config.getStringList("lore"))
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
			minXP = config.getInt("min-xp", 0);
			maxXP = config.getInt("max-xp", 0);
			requiredPower = config.getInt("required-power", 0);
			templateName = config.getString("gen-template", "");
		}
		else {
			blockName = ChatColor.RESET + "Custom Block";
			templateName = "";
			minXP = 0; maxXP = 0; requiredPower = 0;
		}
	}

	//Gets a new CustomBlock instance from a mushroom blockstate.
	public static CustomBlock getFromData(BlockData data) {
		if(!(data instanceof MultipleFacing)) return null;
		MultipleFacing mfData = (MultipleFacing) data;
		MushroomState state = new MushroomState(data.getMaterial(), mfData.hasFace(BlockFace.UP), mfData.hasFace(BlockFace.DOWN),
			mfData.hasFace(BlockFace.WEST), mfData.hasFace(BlockFace.EAST), mfData.hasFace(BlockFace.SOUTH), mfData.hasFace(BlockFace.NORTH));
		
		BlockManager manager = MMOItems.plugin.getCustomBlocks();
		return manager.isVanilla(state) ? null : manager.getBlock(state);
	}

	public int getId()
	{ return id; }
	public String getName()
	{ return blockName; }
	public String getTemplateName()
	{ return templateName; }
	public List<String> getLore()
	{ return lore; }
	public int getMinXPDrop()
	{ return minXP; }
	public int getMaxXPDrop()
	{ return maxXP; }
	public int getRequiredPower()
	{ return requiredPower; }
}
