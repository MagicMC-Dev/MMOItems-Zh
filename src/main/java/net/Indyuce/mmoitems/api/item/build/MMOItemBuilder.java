package net.Indyuce.mmoitems.api.item.build;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat.StringData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MMOItemBuilder {
	private final MMOItem mmoitem;

	private ItemStack item = new ItemStack(VersionMaterial.NETHER_WART.toMaterial());
	private ItemMeta meta;
	private MMOItemLore lore = new MMOItemLore();
	private List<ItemTag> tags = new ArrayList<>();

	private static final AttributeModifier fakeModifier = new AttributeModifier(UUID.fromString("87851e28-af12-43f6-898e-c62bde6bd0ec"), "generic.attackSpeed", 0, Operation.ADD_NUMBER);

	public MMOItemBuilder(MMOItem item) {
		mmoitem = item;

		tags.add(new ItemTag("MMOITEMS_ITEM_TYPE", item.getType().getId()));
		tags.add(new ItemTag("MMOITEMS_ITEM_ID", item.getId()));

		String path = item.getType().getId() + "." + item.getId();
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

	public Material getMaterial() {
		return item.getType();
	}

	public void setMaterial(Material material) {
		item.setType(material);
		(meta = item.getItemMeta()).addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
	}

	public ItemStack build() {

		for (ItemStat stat : mmoitem.getStats())
			stat.whenApplied(this, mmoitem.getData(stat));

		// lore
		if (mmoitem.getType() == Type.GEM_STONE)
			lore.insert("gem-stone-lore", ItemStat.translate("gem-stone-lore"));
		lore.insert("item-type", ItemStat.translate("item-type").replace("#", mmoitem.getStats().contains(ItemStat.DISPLAYED_TYPE) ? ((StringData) mmoitem.getData(ItemStat.DISPLAYED_TYPE)).toString() : mmoitem.getType().getName()));

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
