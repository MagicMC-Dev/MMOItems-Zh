package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GrantedPermissions extends StringListStat implements GemStoneStat {
	public GrantedPermissions() {
		super("GRANTED_PERMISSIONS", new ItemStack(Material.NAME_TAG), "Granted Permissions", new String[] { "A list of permissions that will,", "be granted by the item." }, new String[] { "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.GRANTED_PERMISSIONS).enable("Write in the chat the permission you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains(getPath())) {
			List<String> permissions = inv.getEditedSection().getStringList(getPath());
			if (permissions.isEmpty())
				return;

			String last = permissions.get(permissions.size() - 1);
			permissions.remove(last);
			inv.getEditedSection().set(getPath(), permissions.isEmpty() ? null : permissions);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + MMOLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		List<String> permissions = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList(getPath()) : new ArrayList<>();
		permissions.add(message);
		inv.getEditedSection().set(getPath(), permissions);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Permission successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) statData;
			data.getList().forEach(element -> lore.add(ChatColor.GRAY + MMOLib.plugin.parseColors(element)));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a permission.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last permission.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(array::add);
		item.addItemTag(new ItemTag(getPath(), array.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(ItemStats.GRANTED_PERMISSIONS, new StringListData(
				new JsonParser().parse(mmoitem.getNBT().getString(getNBTPath())).getAsJsonArray()));
	}
}
