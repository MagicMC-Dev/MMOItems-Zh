package net.Indyuce.mmoitems.api.block;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.MushroomState;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.inventory.ItemStack;

public class CustomBlock {
	private final int id;
	private final MushroomState state;

	private final MMOItem mmoitem;

	private final WorldGenTemplate template;
	private final int minExp, maxExp, requiredPower;

	public CustomBlock(MushroomState state, MMOItem mmoitem) {
		this.mmoitem = mmoitem;

		this.id = (mmoitem.hasData(ItemStat.BLOCK_ID)) ? (int) ((DoubleData) mmoitem.getData(ItemStat.BLOCK_ID)).getValue() : 0;
		this.state = state;

		this.minExp = (mmoitem.hasData(ItemStat.MIN_XP)) ? (int) ((DoubleData) mmoitem.getData(ItemStat.MIN_XP)).getValue() : 0;
		this.maxExp = (mmoitem.hasData(ItemStat.MAX_XP)) ? (int) ((DoubleData) mmoitem.getData(ItemStat.MAX_XP)).getValue() : 0;
		this.requiredPower = (mmoitem.hasData(ItemStat.REQUIRED_POWER)) ? (int) ((DoubleData) mmoitem.getData(ItemStat.REQUIRED_POWER)).getValue()
				: 0;

		this.template = (mmoitem.hasData(ItemStat.GEN_TEMPLATE))
				? MMOItems.plugin.getWorldGen().getOrThrow((mmoitem.getData(ItemStat.GEN_TEMPLATE)).toString())
				: null;
	}

	public int getId() {
		return id;
	}

	public MushroomState getState() {
		return state;
	}

	public boolean hasGenTemplate() {
		return template != null;
	}

	public WorldGenTemplate getGenTemplate() {
		return template;
	}

	public int getMinExpDrop() {
		return minExp;
	}

	public int getMaxExpDrop() {
		return maxExp;
	}

	public int getRequiredPower() {
		return requiredPower;
	}

	public ItemStack getItem() {
		return mmoitem.newBuilder().build();
	}
}
