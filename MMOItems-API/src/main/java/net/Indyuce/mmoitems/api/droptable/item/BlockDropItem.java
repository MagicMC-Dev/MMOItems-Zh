package net.Indyuce.mmoitems.api.droptable.item;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class BlockDropItem extends DropItem {
	private final int blockId;

	public BlockDropItem(int blockId, String info) {
		super(info);

		this.blockId = blockId;
	}

	public int getBlockId() {
		return blockId;
	}

	@Override
	public ItemStack getItem(PlayerData player, int amount) {
		return MMOItems.plugin.getCustomBlocks().getBlock(blockId).getItem();
	}

	@Override
	public String getKey() {
		return "block." + getBlockId();
	}
}
