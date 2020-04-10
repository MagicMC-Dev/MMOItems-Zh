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
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
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
			"generic.attackSpeed", 0, Operation.ADD_NUMBER);

	public MMOItemBuilder(MMOItem mmoitem) {
		this.mmoitem = mmoitem;

		item = new ItemStack(
				mmoitem.hasData(ItemStat.MATERIAL) ? ((MaterialData) mmoitem.getData(ItemStat.MATERIAL)).getMaterial() : Material.DIAMOND_SWORD);
		meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		tags.add(new ItemTag("MMOITEMS_ITEM_TYPE", mmoitem.getType().getId()));
		tags.add(new ItemTag("MMOITEMS_ITEM_ID", mmoitem.getId()));

		String path = mmoitem.getType().getId() + "." + mmoitem.getId();
		if (MMOItems.plugin.getUpdater().hasData(path))
			tags.add(new ItemTag("MMOITEMS_ITEM_UUID", MMOItems.plugin.getUpdater().getData(path).getUniqueId().toString()));
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

	public ItemStack build() {

		for (ItemStat stat : mmoitem.getStats())
			try {
				stat.whenApplied(this, mmoitem.getData(stat));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "An error occured while trying to generate item '" + mmoitem.getId() + "' with stat '"
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
			return MMOLib.plugin.getNMS().getNBTItem(item).addTag(tags).toItem();

			/*
			 * on legacy spigot, it is not required to add a fake modifier to
			 * the modifier list, so just override the string tag and it works
			 * fine.
			 */
		} catch (NoSuchMethodError exception) {
			item.setItemMeta(meta);
			@SuppressWarnings("deprecation")
			NBTItem nbt = MMOLib.plugin.getNMS().getNBTItem(item).cancelVanillaAttributeModifiers();
			return nbt.addTag(tags).toItem();
		}
	}
}
