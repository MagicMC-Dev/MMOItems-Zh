package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
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
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class CustomBlock {
	private final MushroomState state;

	private final int id;
	private final String blockName;
	private final String templateName;
	private final List<String> lore = new ArrayList<String>();
	private final int minExp;
	private final int maxExp;
	private final int requiredPower;

	public CustomBlock(int id, MushroomState state, ConfigurationSection config) {
		this.id = id;
		this.state = state;

		Validate.notNull(config, "Could not read custom block config");

		blockName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", ChatColor.RESET + "Custom Block"));
		if (config.contains("lore"))
			for (String s : config.getStringList("lore"))
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
		minExp = config.getInt("min-xp", 0);
		maxExp = config.getInt("max-xp", 0);
		requiredPower = config.getInt("required-power", 0);
		templateName = config.getString("gen-template", "");

		MMOItems.plugin.getWorldGen().register(this);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return blockName;
	}

	public String getTemplateName() {
		return templateName;
	}

	public List<String> getLore() {
		return lore;
	}

	public int getMinXPDrop() {
		return minExp;
	}

	public int getMaxXPDrop() {
		return maxExp;
	}

	public int getRequiredPower() {
		return requiredPower;
	}

	// Depending on the id, return the mushroom type this block
	// is supposed to feature.
	public Material getType() {
		return MMOItems.plugin.getCustomBlocks().getType(id);
	}

	// From the Id, check which sides to apply data to.
	// This will return the blockstate of the blocks id.
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

	// Convert block data into Item
	public ItemStack getItem() {
		ItemStack item = new ItemStack(Material.CLAY_BALL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(blockName);
		meta.setLore(lore);
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.values());

		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 13))
			((Damageable) meta).setDamage(id);

		item.setItemMeta(meta);

		return MMOLib.plugin.getNMS().getNBTItem(item).addTag(new ItemTag("MMOITEMS_DISABLE_CRAFTING", true), new ItemTag("MMOITEMS_DISABLE_SMITHING", true), new ItemTag("MMOITEMS_DISABLE_ENCHANTING", true), new ItemTag("MMOITEMS_DISABLE_REPAIRING", true), new ItemTag("MMOITEMS_BLOCK_ID", id), new ItemTag("CustomModelData", id + 1000)).toItem();
	}

	// Gets a new CustomBlock instance from a mushroom blockstate.
	public static CustomBlock getFromData(BlockData data) {
		if (!MMOItems.plugin.getCustomBlocks().isMushroomBlock(data.getMaterial()))
			return null;
		if (!(data instanceof MultipleFacing))
			return null;
		MultipleFacing mfData = (MultipleFacing) data;
		MushroomState state = new MushroomState(data.getMaterial(), mfData.hasFace(BlockFace.UP), mfData.hasFace(BlockFace.DOWN), mfData.hasFace(BlockFace.WEST), mfData.hasFace(BlockFace.EAST), mfData.hasFace(BlockFace.SOUTH), mfData.hasFace(BlockFace.NORTH));

		BlockManager manager = MMOItems.plugin.getCustomBlocks();
		return manager.isVanilla(state) ? null : manager.getBlock(state);
	}
}
