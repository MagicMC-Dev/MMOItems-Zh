package net.Indyuce.mmoitems.api.item.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.StoredTagsData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.asangarin.hexcolors.ColorParse;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemBuilder {
	private MMOItem mmoitem;

	private final ItemStack item;
	private final ItemMeta meta;
	private final MMOItemLore lore = new MMOItemLore();
	private final List<ItemTag> tags = new ArrayList<>();

	private static final AttributeModifier fakeModifier = new AttributeModifier(UUID.fromString("87851e28-af12-43f6-898e-c62bde6bd0ec"),
			"mmoitemsDecoy", 0, Operation.ADD_NUMBER);

	/***
	 * Used to build an MMOItem into an ItemStack
	 * 
	 * @param mmoitem
	 *            The mmoitem you want to build
	 */
	public MMOItemBuilder(MMOItem mmoitem) {
		this.mmoitem = mmoitem;

		item = new ItemStack(
				mmoitem.hasData(ItemStat.MATERIAL) ? ((MaterialData) mmoitem.getData(ItemStat.MATERIAL)).getMaterial() : Material.DIAMOND_SWORD);
		meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		tags.add(new ItemTag("MMOITEMS_ITEM_TYPE", mmoitem.getType().getId()));
		tags.add(new ItemTag("MMOITEMS_ITEM_ID", mmoitem.getId()));

		if (MMOItems.plugin.getUpdater().hasData(mmoitem))
			tags.add(new ItemTag("MMOITEMS_ITEM_UUID", MMOItems.plugin.getUpdater().getData(mmoitem).getUniqueId().toString()));
	}

	public MMOItemLore getLore() {
		return lore;
	}

	public MMOItem getMMOItem() {
		return mmoitem;
	}

	public ItemStack getItemStack() {
		return item;
	}

	public ItemMeta getMeta() {
		return meta;
	}

	public void addItemTag(ItemTag... itemTags) {
		for (ItemTag itemTag : itemTags)
			tags.add(itemTag);
	}

	public NBTItem buildNBT() {
		this.mmoitem = new StatLore(mmoitem).generateNewItem();

		for (ItemStat stat : mmoitem.getStats())
			try {
				stat.whenApplied(this, mmoitem.getData(stat));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "An error occurred while trying to generate item '" + mmoitem.getId() + "' with stat '"
						+ stat.getId() + "': " + exception.getMessage());
			}

		// lore
		if (mmoitem.getType() == Type.GEM_STONE)
			lore.insert("gem-stone-lore", ItemStat.translate("gem-stone-lore"));
		lore.insert("item-type",
				ItemStat.translate("item-type").replace("#",
						mmoitem.getStats().contains(ItemStat.DISPLAYED_TYPE) ? ((StringData) mmoitem.getData(ItemStat.DISPLAYED_TYPE)).toString()
								: mmoitem.getType().getName()));

		meta.setLore(lore.build().toStringList());

		/*
		 * this tag is added to entirely override default vanilla item attribute
		 * modifiers, this way armor gives no ARMOR or ARMOR TOUGHNESS to the
		 * holder. since 4.7 attributes are handled via custom calculations
		 */
		try {

			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, fakeModifier);
			item.setItemMeta(meta);
			return MMOLib.plugin.getNMS().getNBTItem(item).addTag(tags);

			/*
			 * on legacy spigot, it is not required to add a fake modifier to
			 * the modifier list, so just override the string tag and it works
			 * fine.
			 */
		} catch (NoSuchMethodError exception) {
			item.setItemMeta(meta);
			@SuppressWarnings("deprecation")
			NBTItem nbt = MMOLib.plugin.getNMS().getNBTItem(item).cancelVanillaAttributeModifiers();
			return nbt.addTag(tags);
		}
	}

	/**
	 * @return Builds an ItemStack
	 */
	public ItemStack build() {
		return buildNBT().toItem();
	}

	public class StatLore {
		private final MMOItem mmoitem;

		private final UpgradeData upgradeData;

		/***
		 * @deprecated will be improved with mmoitems 6
		 * 
		 * @param mmoitem
		 */
		public StatLore(MMOItem mmoitem) {
			this.mmoitem = mmoitem.clone();
			this.upgradeData = ((UpgradeData) mmoitem.getData(ItemStat.UPGRADE));
		}

		public MMOItem getMMOItem() {
			return mmoitem;
		}

		public boolean isUpgradable() {
			if (upgradeData != null)
				return upgradeData.getTemplate() != null;
			return false;
		}

		public MMOItem generateNewItem() {
			if (MMOItems.plugin.getConfig().getBoolean("item-upgrading.display-stat-changes", false) && isUpgradable()) {
				if (upgradeData.getLevel() > 0)
					for (ItemStat stat : upgradeData.getTemplate().getKeys()) {
						UpgradeInfo upgradeInfo = upgradeData.getTemplate().getUpgradeInfo(stat);
						if (upgradeInfo instanceof DoubleStat.DoubleUpgradeInfo) {

							DoubleStat.DoubleUpgradeInfo info = ((DoubleStat.DoubleUpgradeInfo) upgradeInfo);
							int level = upgradeData.getLevel();

							if (!mmoitem.hasData(stat))
								mmoitem.setData(stat, new DoubleData(0));

							calculateBase(stat, info, level);

							updateStat(stat, info, level);

							double value = getValue(stat);

							if (value > 0)
								lore.insert(stat.getPath(), stat.format(value, "#", new StatFormat("##").format(value))
										+ new ColorParse(MMOItems.plugin.getConfig().getString("item-upgrading.stat-change-suffix", " &e(+#stat#)")
												.replace("#stat#", new StatFormat("##").format(value - getBase(stat)))).toChatColor());
						}
					}
			}
			return mmoitem;
		}

		public void calculateBase(ItemStat stat, DoubleStat.DoubleUpgradeInfo info, int level) {
			if (!hasBase(stat)) {
				ItemTag tag;
				String key = "BASE_" + stat.getNBTPath();
				double value = getValue(stat);

				// does inverse math to get the base
				if (info.isRelative()) {
					double upgradeAmount = ((DoubleStat.DoubleUpgradeInfo) upgradeData.getTemplate().getUpgradeInfo(stat)).getAmount();

					for (int i = 1; i <= level; i++) {
						value /= 1 + upgradeAmount;
					}

					tag = new ItemTag(key, value);
				} else {
					tag = new ItemTag(key, Math.max(0, value - (info.getAmount() * level)));
				}
				StoredTagsData tagsData = (StoredTagsData) mmoitem.getData(ItemStat.STORED_TAGS);

				tagsData.addTag(tag);
				mmoitem.replaceData(ItemStat.STORED_TAGS, tagsData);
			}
		}

		// sets the mmoitem data to reflect current upgrade
		public void updateStat(ItemStat stat, DoubleStat.DoubleUpgradeInfo info, int level) {
			double base = getBase(stat);
			if (info.isRelative()) {
				for (int i = 1; i <= level; i++) {
					base *= 1 + info.getAmount();
				}
				mmoitem.replaceData(stat, new DoubleData(base));
			} else {
				mmoitem.replaceData(stat, new DoubleData((info.getAmount() * level) + base));
			}
		}

		public HashMap<String, ItemTag> getStoredTags() {
			HashMap<String, ItemTag> map = new HashMap<>();
			StoredTagsData tagsData = (StoredTagsData) mmoitem.getData(ItemStat.STORED_TAGS);

			for (ItemTag tag : tagsData.getTags())
				map.put(tag.getPath(), tag);
			return map;
		}

		public double getValue(ItemStat stat) {
			return ((DoubleData) mmoitem.getData(stat)).generateNewValue();
		}

		public boolean hasBase(ItemStat stat) {
			return getStoredTags().containsKey("BASE_" + stat.getNBTPath());
		}

		public double getBase(ItemStat stat) {
			return Double.parseDouble(getStoredTags().get("BASE_" + stat.getNBTPath()).getValue().toString());
		}
	}

}
