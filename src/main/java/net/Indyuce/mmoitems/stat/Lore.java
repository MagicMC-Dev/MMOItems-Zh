package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Lore extends ItemStat implements ProperStat {
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
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.LORE).enable("Write in the chat the lore line you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("lore")) {
				List<String> lore = config.getConfig().getStringList(inv.getEdited().getId() + ".lore");
				if (lore.size() < 1)
					return;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				config.getConfig().set(inv.getEdited().getId() + ".lore", lore);
				inv.registerTemplateEdition(config, true);
				inv.open();
				inv.getPlayer().sendMessage(
						MMOItems.plugin.getPrefix() + "Successfully removed '" + MMOLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
			}
		}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("lore")
				? config.getConfig().getStringList(inv.getEdited().getId() + ".lore")
				: new ArrayList<>();
		lore.add(message);
		config.getConfig().set(inv.getEdited().getId() + ".lore", lore);
		inv.registerTemplateEdition(config, true);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) mmoitem.getData(this);
			data.getList().forEach(element -> lore.add(ChatColor.GRAY + MMOLib.plugin.parseColors(element)));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a line.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last line.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		List<String> lore = new ArrayList<>();
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(line -> {
			line = MMOLib.plugin.parseColors(line);
			array.add(line);
			lore.add(line);
		});
		item.getLore().insert("lore", lore);
		item.addItemTag(new ItemTag("MMOITEMS_LORE", array.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_LORE"))
			mmoitem.setData(ItemStat.LORE, new StringListData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_LORE")).getAsJsonArray()));
	}
}
