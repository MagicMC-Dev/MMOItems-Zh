package net.Indyuce.mmoitems.api.item.build;

import com.google.gson.JsonArray;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.utils.text.Component;
import io.lumine.mythic.utils.text.format.TextDecoration;
import io.lumine.mythic.utils.text.minimessage.MiniMessage;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.util.DynamicLore;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.StoredTagsData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ItemStackBuilder {
	private MMOItem mmoitem;

	private final ItemStack item;
	private final ItemMeta meta;
	private final LoreBuilder lore;
	private final List<ItemTag> tags = new ArrayList<>();

	private static final AttributeModifier fakeModifier = new AttributeModifier(
			UUID.fromString("87851e28-af12-43f6-898e-c62bde6bd0ec"), "mmoitemsDecoy", 0, Operation.ADD_NUMBER);

	/**
	 * Used to build an MMOItem into an ItemStack.
	 * 
	 * @param mmoitem The mmoitem you want to build
	 */
	public ItemStackBuilder(MMOItem mmoitem) {

		// Reference to source MMOItem
		this.mmoitem = mmoitem;

		// Generates a new ItemStack of the specified material (Specified in the Material stat, or a DIAMOND_SWORD if missing).
		item = new ItemStack( mmoitem.hasData(ItemStats.MATERIAL) ?
						((MaterialData) mmoitem.getData(ItemStats.MATERIAL)).getMaterial()
						: Material.DIAMOND_SWORD);

		// Gets a lore builder, which will be used to apply the chosen lore format (Choose with the lore format stat, or the default one if unspecified)
		lore = new LoreBuilder(mmoitem.hasData(ItemStats.LORE_FORMAT)
				? MMOItems.plugin.getFormats().getFormat(mmoitem.getData(ItemStats.LORE_FORMAT).toString())
				: MMOItems.plugin.getLanguage().getDefaultLoreFormat());

		// Gets the meta, and hides attributes
		meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		// Store the internal TYPE-ID Information (not stats, so it must be done manually here)
		tags.add(new ItemTag("MMOITEMS_ITEM_TYPE", mmoitem.getType().getId()));
		tags.add(new ItemTag("MMOITEMS_ITEM_ID", mmoitem.getId()));

		// And a last technical tag for updating items
		if (MMOItems.INTERNAL_REVISION_ID > 1) { tags.add(new ItemTag("MMOITEMS_INTERNAL_REVISION_ID", MMOItems.INTERNAL_REVISION_ID)); }
		/*if (MMOItems.plugin.getUpdater().hasData(mmoitem))
			tags.add(new ItemTag("MMOITEMS_ITEM_UUID",
					MMOItems.plugin.getUpdater().getData(mmoitem.getType(), mmoitem.getId()).getUniqueId().toString()));*/
	}

	public LoreBuilder getLore() {
		return lore;
	}

	public MMOItem getMMOItem() {
		return mmoitem;
	}

	/**
	 * @return Does NOT return the built item stack. It returns only returns the
	 *         default item stack with material applied. Built item stack is given
	 *         by build(). This method should only be used to check if the item is
	 *         of a specific material (like the Shield Pattern stat which checks if
	 *         the item is a shield)
	 */
	public ItemStack getItemStack() {
		return item;
	}

	public ItemMeta getMeta() {
		return meta;
	}


	public void addItemTag(List<ItemTag> newTags) { tags.addAll(newTags); }
	public void addItemTag(ItemTag... itemTags) { tags.addAll(Arrays.asList(itemTags)); }

	public static final String histroy_keyword = "HSTRY_";

	/**
	 * @return Returns built NBTItem with applied tags and lore
	 */
	public NBTItem buildNBT() {
		this.mmoitem = new StatLore(mmoitem).generateNewItem();

		//GEM//MMOItems.Log("\u00a7e+ \u00a77Building \u00a7c" + mmoitem.getType().getName() + " " + mmoitem.getId() + "\u00a77 (Size \u00a7e" + mmoitem.mergeableStatHistory.size() + "\u00a77 Historic)");

		// For every stat within this item
		for (ItemStat stat : mmoitem.getStats())

			// Attempt to add
			try {

				//GEM//MMOItems.Log("\u00a7e -+- \u00a77Applying \u00a76" + stat.getNBTPath());

				// Make necessary lore changes
				stat.whenApplied(this, mmoitem.getData(stat));

				// Does the item have any stat history regarding thay?
				StatHistory<StatData> s = mmoitem.getStatHistory(stat);

				// Found it?
				if (s != null) {

					//GEM//MMOItems.Log("\u00a7a -+- \u00a77Found History");

					// Add to NBT
					addItemTag(new ItemTag(histroy_keyword + stat.getId(), s.toNBTString()));
				}

			// Something went wrong...
			} catch (IllegalArgumentException|NullPointerException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						ChatColor.GRAY + "An error occurred while trying to generate item '"
						+ ChatColor.RED + mmoitem.getId() + ChatColor.GRAY + "' with stat '"
						+ ChatColor.GOLD + stat.getId() + ChatColor.GRAY + "': "
						+ ChatColor.YELLOW + exception.getMessage());
			}

		// Display gem stone lore hint thing
		if (mmoitem.getType() == Type.GEM_STONE)
			lore.insert("gem-stone-lore", ItemStat.translate("gem-stone-lore"));

		// Display item type
		lore.insert("item-type",
				ItemStat.translate("item-type").replace("#",
						mmoitem.getStats().contains(ItemStats.DISPLAYED_TYPE)
								? mmoitem.getData(ItemStats.DISPLAYED_TYPE).toString()
								: mmoitem.getType().getName()));

		// Calculate lore with placeholders
		if (mmoitem.hasData(ItemStats.LORE)) {
			List<String> parsed = new ArrayList<>();
			((StringListData) mmoitem.getData(ItemStats.LORE)).getList()
					.forEach(str -> parsed.add(lore.applyLorePlaceholders(str)));
			lore.insert("lore", parsed);
		}

		final List<String> list = lore.build();
		JsonArray array = new JsonArray();
		for (String s : list)
			array.add(s);
		if(array.size() != 0)
			tags.add(new ItemTag("MMOITEMS_DYNAMIC_LORE", array.toString()));
		meta.setLore(list);

		/*
		 * This tag is added to entirely override default vanilla item attribute
		 * modifiers, this way armor gives no ARMOR or ARMOR TOUGHNESS to the holder.
		 * Since 4.7 attributes are handled via custom calculations
		 */
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, fakeModifier);

		item.setItemMeta(meta);
		NBTItem nbtItem = NBTItem.get(item);

		nbtItem.addTag(tags);

		nbtItem.setDisplayNameComponent(Component.text()
				.append(MiniMessage.get().parse(meta.getDisplayName()))
				.decoration(TextDecoration.ITALIC, false)
				.build());

		return nbtItem;
	}

	/**
	 * @return Builds the item
	 */
	public ItemStack build() {
		return new DynamicLore(buildNBT()).build();
	}

	public class StatLore {
		private final MMOItem mmoitem;

		private final UpgradeData upgradeData;

		/**
		 * @deprecated Will be improved with MMOItems 7. Currently used to display
		 *             upgrade stats in the lore. Should be improved to be more OOP
		 *             friendly. NO MODIFICATIONS ALLOWED BEFORE A REWRITE
		 */
		public StatLore(MMOItem mmoitem) {
			this.mmoitem = mmoitem.clone();
			this.upgradeData = (UpgradeData) mmoitem.getData(ItemStats.UPGRADE);
		}

		public MMOItem getMMOItem() {
			return mmoitem;
		}

		public boolean isUpgradable() {
			return upgradeData != null && upgradeData.getTemplate() != null;
		}

		public MMOItem generateNewItem() {
			if (MMOItems.plugin.getConfig().getBoolean("item-upgrading.display-stat-changes", false) && isUpgradable()) {

				if (upgradeData.getLevel() > 0)

					for (ItemStat stat : upgradeData.getTemplate().getKeys()) {

						UpgradeInfo upgradeInfo = upgradeData.getTemplate().getUpgradeInfo(stat);
						if (upgradeInfo instanceof DoubleStat.DoubleUpgradeInfo) {

							DoubleStat.DoubleUpgradeInfo info = ((DoubleStat.DoubleUpgradeInfo) upgradeInfo);
							int level = upgradeData.getLevel();

							if (!mmoitem.hasData(stat)){

								mmoitem.setData(stat, new DoubleData(0));
							}

							calculateBase(stat, info, level);
							updateStat(stat, info, level);

							double value = getValue(stat);

							if (value > 0)
								lore.insert(stat.getPath(), stat.formatNumericStat(value, "#",
										new StatFormat("##").format(value))
										+ MythicLib.plugin.parseColors(MMOItems.plugin.getConfig()
												.getString("item-upgrading.stat-change-suffix", " &e(+#stat#)").replace(
														"#stat#", new StatFormat("##").format(value - getBase(stat)))));
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
					double upgradeAmount = ((DoubleStat.DoubleUpgradeInfo) upgradeData.getTemplate()
							.getUpgradeInfo(stat)).getAmount();

					for (int i = 1; i <= level; i++) {
						value /= 1 + upgradeAmount;
					}

					tag = new ItemTag(key, value);
				} else {
					tag = new ItemTag(key, Math.max(0, value - (info.getAmount() * level)));
				}
				StoredTagsData tagsData = (StoredTagsData) mmoitem.getData(ItemStats.STORED_TAGS);

				tagsData.addTag(tag);
				mmoitem.replaceData(ItemStats.STORED_TAGS, tagsData);
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
			StoredTagsData tagsData = (StoredTagsData) mmoitem.getData(ItemStats.STORED_TAGS);

			for (ItemTag tag : tagsData.getTags())
				map.put(tag.getPath(), tag);
			return map;
		}

		public double getValue(ItemStat stat) {
			return ((DoubleData) mmoitem.getData(stat)).getValue();
		}

		public boolean hasBase(ItemStat stat) {
			return getStoredTags().containsKey("BASE_" + stat.getNBTPath());
		}

		public double getBase(ItemStat stat) {
			return Double.parseDouble(getStoredTags().get("BASE_" + stat.getNBTPath()).getValue().toString());
		}
	}
}
