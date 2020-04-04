package net.Indyuce.mmoitems.comp.mmocore.load;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomBlock;
import net.mmogroup.mmolib.MMOLib;

public class MMOItemsBlockType implements BlockType {
	private final int id;

	public MMOItemsBlockType(MMOLineConfig config) {
		config.validate("id");

		id = config.getInt("id");
	}

	public MMOItemsBlockType(Block block) {
		id = CustomBlock.getFromData(block.getBlockData()).getId();
	}

	public int getBlockId() {
		return id;
	}

	@Override
	public void place(Location loc, RegeneratingBlock regenerating) {
		CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(id);
		loc.getBlock().setType(block.getType());
		if (MMOLib.plugin.getVersion().isStrictlyHigher(1, 12))
			loc.getBlock().setBlockData(block.getBlockData());
	}

	public static boolean matches(Block block) {
		return MMOItems.plugin.getCustomBlocks().isMushroomBlock(block.getType());
	}

	@Override
	public String generateKey() {
		return "mmoitems-custom-block-" + id;
	}
}
