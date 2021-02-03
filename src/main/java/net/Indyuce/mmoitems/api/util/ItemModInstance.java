package net.Indyuce.mmoitems.api.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import io.lumine.mythic.lib.api.item.NBTItem;

public class ItemModInstance {
	private final NBTItem nbtItem;
	private final int amount;

	private final Map<ItemStat, StatData> itemData = new HashMap<>();

	// Not initialized at first for performance reasons
	private MMOItem mmoItem;

	private String cachedName;
	private List<String> cachedLore;
	private Map<Enchantment, Integer> cachedEnchants;
	private StatData cachedSoulbound;

	public ItemModInstance(NBTItem nbt) {
		this.nbtItem = nbt;
		this.amount = nbt.getItem().getAmount();
	}

	public void applySoulbound(Player p) {
		applySoulbound(p, MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1));
	}

	public void applySoulbound(Player p, int level) {
		loadLiveMMOItem();
		mmoItem.setData(ItemStats.SOULBOUND, new SoulboundData(p.getUniqueId(), p.getName(), level));
	}

	public void reforge(Player p, ReforgeOptions options) {
		reforge(p == null ? null : PlayerData.get(p).getRPG(), options);
	}

	/**
	 * @param player The player to generate the new item from. If empty, it will
	 *               use the old items level and tier, or default values if
	 *               needed.
	 */
	public void reforge(RPGPlayer player, ReforgeOptions options) {
		loadVolatileMMOItem();
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId());

		ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (options.shouldKeepName() && meta.hasDisplayName())
			cachedName = meta.getDisplayName();
		if (options.shouldKeepLore() && meta.hasLore())
			cachedLore = meta.getLore();
		if (options.shouldKeepEnchantments())
			cachedEnchants = nbtItem.getItem().getEnchantments();

		if (options.shouldKeepModifications())
			for (ItemStat stat : mmoItem.getStats()) {
				if (stat instanceof Upgradable)
					itemData.put(stat, mmoItem.getData(stat));

				if (!(stat instanceof GemStoneStat)) {
					StatData data = mmoItem.getData(stat);
					if (data instanceof Mergeable) {
						if (itemData.containsKey(stat))
							((Mergeable) itemData.get(stat)).merge(data);
						else
							itemData.put(stat, data);
					}
				}
			}

		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND))
			cachedSoulbound = mmoItem.getData(ItemStats.SOULBOUND);

		if (player == null) {
			final int iLevel = MMOItems.plugin.getConfig().getInt("item-revision.default-item-level", -1);
			int level = iLevel == -1
					? (mmoItem.hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) mmoItem.getData(ItemStats.ITEM_LEVEL)).getValue() : 0)
					: iLevel;
			ItemTier tier = (mmoItem.hasData(ItemStats.TIER) && MMOItems.plugin.getConfig().getBoolean("item-revision.keep-tiers"))
					? MMOItems.plugin.getTiers().get(mmoItem.getData(ItemStats.TIER).toString())
					: null;
			mmoItem = template.newBuilder(level, tier).build();
		} else
			mmoItem = template.newBuilder(player).build();
	}

	public boolean hasChanges() {
		return mmoItem != null;
	}

	public ItemStack toStack() {
		for (Map.Entry<ItemStat, StatData> data : itemData.entrySet())
			if (mmoItem.hasData(data.getKey()))
				((Mergeable) mmoItem.getData(data.getKey())).merge(data.getValue());
			else
				mmoItem.setData(data.getKey(), data.getValue());
		if (cachedSoulbound != null)
			mmoItem.setData(ItemStats.SOULBOUND, cachedSoulbound);
		ItemStack stack = mmoItem.newBuilder().build();
		stack.setAmount(amount);
		ItemMeta meta = stack.getItemMeta();
		if (cachedName != null)
			meta.setDisplayName(cachedName);
		if (cachedLore != null)
			meta.setLore(cachedLore);
		stack.setItemMeta(meta);
		if (cachedEnchants != null)
			stack.addUnsafeEnchantments(cachedEnchants);
		return stack;
	}

	/*
	 * Initialize the MMOItem as a LiveMMOItem if it's null or not already a
	 * LiveMMOItem
	 */
	private void loadLiveMMOItem() {
		if (mmoItem != null && mmoItem instanceof LiveMMOItem)
			return;
		mmoItem = new LiveMMOItem(nbtItem);
	}

	/* Initialize the MMOItem as a VolatileMMOItem if it's null */
	private void loadVolatileMMOItem() {
		if (mmoItem != null)
			return;
		mmoItem = new VolatileMMOItem(nbtItem);
	}
}
