package net.Indyuce.mmoitems.api.item.build;

import com.google.gson.JsonArray;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.util.DynamicLore;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
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
		// Clone as to not conflict in any way
		this.mmoitem = mmoitem.clone();
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
		if (array.size() != 0)
			tags.add(new ItemTag("MMOITEMS_DYNAMIC_LORE", array.toString()));

		/*
		 * This tag is added to entirely override default vanilla item attribute
		 * modifiers, this way armor gives no ARMOR or ARMOR TOUGHNESS to the holder.
		 * Since 4.7 attributes are handled via custom calculations
		 */
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, fakeModifier);

		item.setItemMeta(meta);
		NBTItem nbtItem = NBTItem.get(item);

		nbtItem.addTag(tags);

		if (meta.hasDisplayName())
			nbtItem.setDisplayNameComponent(LegacyComponent.parse(meta.getDisplayName()));

		List<Component> componentLore = new ArrayList<>();
		list.forEach(line -> componentLore.add(LegacyComponent.parse(line)));
		nbtItem.setLoreComponents(componentLore);

		return nbtItem;
	}

	/**
	 * @return Builds the item
	 */
	public ItemStack build() {
		return new DynamicLore(buildNBT()).build();
	}
}
