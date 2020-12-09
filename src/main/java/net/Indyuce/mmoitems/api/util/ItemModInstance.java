package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemModInstance {
	private final NBTItem nbtItem;
	private final int amount;
	private final Map<Enchantment, Integer> enchantments;
	// Not initialized at first for performance reasons
	private MMOItem mmoItem;

	public ItemModInstance(NBTItem nbt) {
		this.nbtItem = nbt;
		this.amount = nbt.getItem().getAmount();
		this.enchantments = nbt.getItem().getEnchantments();
	}

	public void applySoulbound(Player p) {
		applySoulbound(p, MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1));
	}

	public void applySoulbound(Player p, int level) {
		loadLiveMMOItem();
		mmoItem.setData(ItemStats.SOULBOUND, ((Soulbound) ItemStats.SOULBOUND).newSoulboundData(p.getUniqueId(), p.getName(), level));
	}

	public void reforge(Player p) {
		reforge(p == null ? null : PlayerData.get(p).getRPG());
	}

	/**
	 * @param player The player to generate the new item from.
	 *               If empty, it will use the old items level
	 *               and tier, or default values if needed.
	 */
	public void reforge(RPGPlayer player) {
		loadVolatileMMOItem();
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId());
		if (player == null) {
			final int iLevel = MMOItems.plugin.getConfig().getInt("item-revision.default-item-level", -1);
			int level = iLevel == -1 ? (mmoItem.hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) mmoItem.getData(ItemStats.ITEM_LEVEL)).getValue() : 0) : iLevel;
			ItemTier tier = mmoItem.hasData(ItemStats.TIER) ? MMOItems.plugin.getTiers().get(mmoItem.getData(ItemStats.TIER).toString()) : null;
			mmoItem = template.newBuilder(level, tier).build();
		} else mmoItem = template.newBuilder(player).build();
	}

	public boolean hasChanges() {
		return mmoItem != null;
	}

	public ItemStack toStack() {
		ItemStack stack = mmoItem.newBuilder().build();
		stack.setAmount(amount);
		stack.addUnsafeEnchantments(this.enchantments);
		return stack;
	}

	/* Initialize the MMOItem as a LiveMMOItem if it's null or not already a LiveMMOItem */
	private void loadLiveMMOItem() {
		if(mmoItem != null && mmoItem instanceof LiveMMOItem) return;
		mmoItem = new LiveMMOItem(nbtItem);
	}

	/* Initialize the MMOItem as a VolatileMMOItem if it's null */
	private void loadVolatileMMOItem() {
		if(mmoItem != null) return;
		mmoItem = new VolatileMMOItem(nbtItem);
	}
}
