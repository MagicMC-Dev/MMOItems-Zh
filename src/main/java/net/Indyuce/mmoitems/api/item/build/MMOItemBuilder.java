package net.Indyuce.mmoitems.api.item.build;

import java.util.ArrayList;
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
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.asangarin.hexcolors.ColorParse;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemBuilder {
	private final MMOItem mmoitem;

	private final ItemStack item;
	private final ItemMeta meta;
	private final MMOItemLore lore = new MMOItemLore();
	private final List<ItemTag> tags = new ArrayList<>();

	private static final AttributeModifier fakeModifier = new AttributeModifier(UUID.fromString("87851e28-af12-43f6-898e-c62bde6bd0ec"),
			"mmoitemsDecoy", 0, Operation.ADD_NUMBER);

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
		
		// TODO MMOItems 6 - scrap that big portion
		if (MMOItems.plugin.getConfig().getBoolean("item-upgrading.display-stat-changes", false))
			if (mmoitem.isUpgradable()) {
				UpgradeData upgradeData = ((UpgradeData) mmoitem.getData(ItemStat.UPGRADE));
				for (ItemStat stat : upgradeData.getTemplate().getKeys()) {
					try {
						if (upgradeData.getLevel() > 0) {

							// data getters
							DoubleStat.DoubleUpgradeInfo doubleUpgradeInfo = ((DoubleStat.DoubleUpgradeInfo) upgradeData.getTemplate().getUpgradeInfo(stat));
							StoredTagsData data = (StoredTagsData) mmoitem.getData(ItemStat.STORED_TAGS);

							if (!mmoitem.hasData(stat))
								mmoitem.setData(stat, new DoubleData(0));

							// default values
							double baseValue = 0;
							boolean hasBase = false;
							double value = ((DoubleData) mmoitem.getData(stat)).generateNewValue();
							int level = upgradeData.getLevel();


							/*
							 * checks if base value is set
							 */
							for (ItemTag tag : data.getTags()) {
								if (tag.getPath().equals("BASE_" + stat.getNBTPath())) {
									hasBase = true;
									baseValue = Double.parseDouble(tag.getValue().toString());
									break;
								}
							}

							/*
							 * sets the base value in stored_tags if it's not set
							 * will also get the correct base for already upgraded items
							 */
							if (!hasBase) // this checks if the base stat is registers
								if (doubleUpgradeInfo != null) {

									ItemTag baseTag;

									if (!doubleUpgradeInfo.isRelative()) {
										baseTag = new ItemTag("BASE_" + stat.getNBTPath(),
												value/level );

									}

									else {
										double base = value;
										for(int i=1;i<=level;i++){
											base /= 1 + doubleUpgradeInfo.getAmount();
										}
										baseTag = new ItemTag("BASE_" + stat.getNBTPath(), base);
									}

									data.addTag(baseTag);
									baseValue = Double.parseDouble(baseTag.getValue().toString());
									mmoitem.replaceData(ItemStat.STORED_TAGS, data);
								}

							/*
							 * updates the stats
							 */
							if (doubleUpgradeInfo != null) // this updates stats to the current version
								if (!doubleUpgradeInfo.isRelative())
									mmoitem.replaceData(stat, new DoubleData((doubleUpgradeInfo.getAmount() * level)
											+ baseValue));
								else if (doubleUpgradeInfo.isRelative()) {
									double base = baseValue;
									for(int i=1;i<=level;i++){
										base *= 1 + doubleUpgradeInfo.getAmount();
									}
									mmoitem.replaceData(stat, new DoubleData(base));
								}

								/*
								 * inserts the correct lore
								 */
							value = ((DoubleData) mmoitem.getData(stat)).generateNewValue();
							if (value > 0)
								lore.insert(stat.getPath(), stat.format(value, "#", new StatFormat("##").format(value))
										+ new ColorParse(MMOItems.plugin.getConfig().getString("item-upgrading.stat-change-suffix", " &e(+#stat#)")
										.replace("#stat#", new StatFormat("##").format(value-baseValue))).toChatColor());
						}
					} catch (IllegalArgumentException exception) {
						MMOItems.plugin.getLogger().log(Level.WARNING, "An error occurred while trying to generate item '" + mmoitem.getId() + "' with stat '"
								+ stat.getId() + "': " + exception.getMessage());
					}
				}
			}
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

	public ItemStack build() {
		return buildNBT().toItem();
	}
}
