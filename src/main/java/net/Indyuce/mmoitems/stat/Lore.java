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
import net.mmogroup.mmolib.version.VersionMaterial;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Lore extends StringListStat implements GemStoneStat {
	public Lore() {
		super("LORE", new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial()), "Lore", new String[] { "The item lore." }, new String[] { "all" });
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
			new StatEdition(inv, ItemStats.LORE).enable("Write in the chat the lore line you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("lore")) {
			List<String> lore = inv.getEditedSection().getStringList("lore");
			if (lore.isEmpty())
				return;

			String last = lore.get(lore.size() - 1);
			lore.remove(last);
			inv.getEditedSection().set("lore", lore.isEmpty() ? null : lore);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + MMOLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		List<String> lore = inv.getEditedSection().contains("lore") ? inv.getEditedSection().getStringList("lore") : new ArrayList<>();
		lore.add(message);
		inv.getEditedSection().set("lore", lore);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore successfully added.");
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
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a line.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last line.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {

		/*
		 * The lore is not directly inserted into the final itemStack lore
		 * because all stats have not registered all their lore placeholders
		 * yet. The lore is only saved in a JSon array so that it can be
		 * recalculated LATER on with right placeholders
		 */

		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(array::add);
		item.addItemTag(new ItemTag("MMOITEMS_LORE", array.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_LORE"))
			mmoitem.setData(ItemStats.LORE, new StringListData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_LORE")).getAsJsonArray()));
	}
}
