package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.asangarin.hexcolors.ColorParse;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class StringStat extends ItemStat {
	public StringStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public StatData whenInitialized(Object object) {
		return new StringData(object.toString());
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
		item.getLore().insert(getPath(), data.toString());
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(inv.getEdited().getId() + "." + getPath(), null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ".");
			return true;
		}
		new StatEdition(inv, this).enable("Write in the chat the text you want.");
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		config.getConfig().set(inv.getEdited().getId() + "." + getPath(), message);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + message + ChatColor.GRAY + ".");
		return true;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new StringData(mmoitem.getNBT().getString(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			String value = new ColorParse('&', mmoitem.getData(this).toString()).toChatColor();
			value = value.length() > 40 ? value.substring(0, 40) + "..." : value;
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + value);

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + " None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return new StringData(object.toString());
	}
}
