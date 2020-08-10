package net.Indyuce.mmoitems.comp.mmocore.load;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MMOItemsBlockType implements BlockType {
	private final int id;

	public MMOItemsBlockType(MMOLineConfig config) {
		config.validate("id");

		id = config.getInt("id");
	}

	public MMOItemsBlockType(CustomBlock block) {
		id = block.getId();
	}

	public int getBlockId() {
		return id;
	}

	@Override
	public void place(Location loc, RegeneratingBlock regenerating) {
		CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(id);
		loc.getBlock().setType(block.getState().getType());
		loc.getBlock().setBlockData(block.getState().getBlockData());
	}

	public static boolean matches(Block block) {
		return MMOItems.plugin.getCustomBlocks().isMushroomBlock(block.getType());
	}

	@Override
	public String generateKey() {
		return "mmoitems-custom-block-" + id;
	}
	
	@Override
	public boolean breakRestrictions(Block block) {
		return true;
	}
}
