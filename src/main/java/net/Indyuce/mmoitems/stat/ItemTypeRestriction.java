package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTypeRestriction extends ItemStat<StringListData, StringListData> {
	public ItemTypeRestriction() {
		super("ITEM_TYPE_RESTRICTION", Material.EMERALD, "Item Type Restriction",
				new String[] { "This option defines the item types", "on which your gem can be applied." }, new String[] { "gem_stone" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.ITEM_TYPE_RESTRICTION).enable("Write in the chat the item type you want your gem to support.",
					"Supported formats: WEAPON or BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA.");

		// if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
		// StatEdition.put(player, new StatEdition(type, path,
		// ItemStat.ITEM_TYPE_RESTRICTION, event.getInventory(), 2));
		// player.closeInventory();
		// PluginInventory.startChatEdition(player, player);
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Write in the chat
		// the value you want to display in the lore.");
		// }
		//
		// if (event.getAction() == InventoryAction.DROP_ONE_SLOT)
		// if (config.getConfigurationSection(path).contains(getPath()))
		// if (config.getConfigurationSection(path + "." +
		// getPath()).contains("display")) {
		// inv.getEditedSection().set(path + "." + getPath() + ".display", null);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset
		// the type restrictions display.");
		// }

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (inv.getEditedSection().contains(getPath())) {
				List<String> list = inv.getEditedSection().getStringList("" + getPath());
				if (list.size() < 1)
					return;

				String last = list.get(list.size() - 1);
				list.remove(last);
				inv.getEditedSection().set("" + getPath(), list.size() == 0 ? null : list);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		// if (counter > 1) {
		// inv.getEditedSection().set(path + "." + getPath() + ".display",
		// message);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Type restrictions
		// display successfully changed to " + message + ChatColor.GRAY + ".");
		// return true;
		// }

		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		Validate.isTrue(isValid(format), format
				+ " is not a valid item type/set. You can enter WEAPON, BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA, as well as other item types here: /mi list type.");

		List<String> list = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList("" + getPath()) : new ArrayList<>();
		list.add(format);
		inv.getEditedSection().set(getPath(), list);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your gem now supports " + format + ".");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) statData.get();
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "Compatible with any type.");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a supported item type/set.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last element.");
	}

	@Override public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {

		// Add NBT
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {

		// Make Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Add that tag
		String joined = String.join(",", ((StringListData) data).getList());
		ret.add(new ItemTag(getNBTPath(), joined));

		// Thats it
		return ret;
	}

	@Override public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		// Add sole tag
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Use that
		StringListData bakedData = (StringListData) getLoadedNBT(relevantTags);

		// Valid?
		if (bakedData != null) {

			// Set
			mmoitem.setData(this, bakedData);
		}
	}
	@Nullable @Override public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// You got a double righ
		ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found righ
		if (tg != null) {

			// Get number
			String value = (String) tg.getValue();
			ArrayList<String> tag = new ArrayList<>();
			if (value.contains(",")) {
				for (String t : value.split(",")) {
					if (!t.isEmpty()) { tag.add(t); } }
			} else { tag.add(value); }

			// That's it
			return new StringListData(tag);
		}

		// Fail
		return null;
	}

	private boolean isValid(String format) {
		if (format.equals("WEAPON"))
			return true;

		for (Type type : MMOItems.plugin.getTypes().getAll())
			if (type.getId().equals(format))
				return true;

		for (TypeSet set : TypeSet.values())
			if (set.name().equals(format))
				return true;

		return false;
	}

	@NotNull
	@Override
	public StringListData getClearStatData() {
		return new StringListData();
	}
}
