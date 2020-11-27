package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemModInstance {
	private final NBTItem nbtItem;
	// Not initialized at first for performance reasons
	private MMOItem mmoItem;

	public ItemModInstance(ItemStack stack) {
		this(MMOLib.plugin.getVersion().getWrapper().getNBTItem(stack));
	}
	public ItemModInstance(NBTItem nbt) {
		this.nbtItem = nbt;
	}

	public ItemModInstance applySoulbound(Player p) {
		return applySoulbound(p, MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1));
	}

	public ItemModInstance applySoulbound(Player p, int level) {
		getMMO().setData(ItemStats.SOULBOUND, ((Soulbound) ItemStats.SOULBOUND).newSoulboundData(p.getUniqueId(), p.getName(), level));
		return this;
	}

	public ItemModInstance reforge(Player p) {
		return reforge(PlayerData.get(p).getRPG());
	}

	/**
	 * @param player The player to generate the new item from.
	 *               If empty, it will use the old items level
	 *               and tier, or default values if needed.
	 */
	public ItemModInstance reforge(RPGPlayer player) {
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(getMMO().getType(), getMMO().getId());
		if(player == null) {
			int level = getMMO().hasData(ItemStats.ITEM_LEVEL) ? (int)
					((DoubleData) getMMO().getData(ItemStats.ITEM_LEVEL)).getValue() : 1;
			ItemTier tier = MMOItems.plugin.getTiers().get(getMMO().getData(ItemStats.TIER).toString());
			mmoItem = template.newBuilder(level, tier).build();
		} else mmoItem = template.newBuilder(player).build();
		return this;
	}

	public boolean hasChanges() {
		return mmoItem != null;
	}

	public ItemStack toStack() {
		return mmoItem.newBuilder().build();
	}

	private MMOItem getMMO() {
		return mmoItem == null ? mmoItem = new VolatileMMOItem(nbtItem) : mmoItem;
	}
}
