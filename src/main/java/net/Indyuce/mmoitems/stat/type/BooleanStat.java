package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.random.RandomBooleanData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class BooleanStat extends ItemStat {
	public BooleanStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {

		if (object instanceof Boolean)
			return new RandomBooleanData((boolean) object);

		if (object instanceof Number)
			return new RandomBooleanData(Double.valueOf(object.toString()));

		throw new IllegalArgumentException("Must specify a number (chance) or true/false");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled()) {
			item.addItemTag(new ItemTag(getNBTPath(), true));
			item.getLore().insert(getPath(), MMOItems.plugin.getLanguage().getStatFormat(getPath()));
		}
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		config.getConfig().set(inv.getEdited().getId() + "." + getPath(), !config.getConfig().getBoolean(inv.getEdited().getId() + "." + getPath()));
		inv.registerTemplateEdition(config);
		inv.open();
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		return true;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new BooleanData(mmoitem.getNBT().getBoolean(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {
		lore.add(ChatColor.GRAY + "Current Value: "
				+ (mmoitem.hasData(this) && ((BooleanData) mmoitem.getData(this)).isEnabled() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to switch this value.");
	}
}
