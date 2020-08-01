package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DoubleStat extends ItemStat implements Upgradable {
	public DoubleStat(String id, ItemStack item, String name, String[] lore) {
		super(id, item, name, lore, new String[] { "!miscellaneous", "all" });
	}

	public DoubleStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public StatData whenInitialized(Object object) {
		return new DoubleData(object);
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {

		if (object instanceof Number)
			return new NumericStatFormula(Double.valueOf(object.toString()), 0, 0, 0);

		if (object instanceof ConfigurationSection)
			return new NumericStatFormula((ConfigurationSection) object);

		throw new IllegalArgumentException("Must specify a number or a config section");
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag(getNBTPath(), value));
		if (value > 0)
			item.getLore().insert(getPath(), format(value, "#", new StatFormat("##").format(value)));
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = inv.getEdited().getType().getConfigFile();
			config.getConfig().set(inv.getEdited().getId() + "." + getPath(), null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ChatColor.GRAY + ".");
			return true;
		}
		new StatEdition(inv, this).enable("Write in the chat the numeric value you want.",
				"Or write [MIN-VALUE]=[MAX-VALUE] to make the stat random.");
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\=");
		double value = 0;
		double value1 = 0;
		try {
			value = Double.parseDouble(split[0]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[0] + " is not a valid number.");
			return false;
		}

		// second value
		if (split.length > 1)
			try {
				value1 = Double.parseDouble(split[1]);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number.");
				return false;
			}

		// STRING if length == 2
		// DOUBLE if length == 1
		config.getConfig().set(inv.getEdited().getId() + "." + getPath(), split.length > 1 ? value + "=" + value1 : value);
		if (value == 0 && value1 == 0)
			config.getConfig().set(inv.getEdited().getId() + "." + getPath(), null);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to "
				+ (value1 != 0 ? "{between " + value + " and " + value1 + "}" : "" + value) + ".");
		return true;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getDouble(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		DoubleData data;
		String format = mmoitem.hasData(this)
				? ((data = (DoubleData) mmoitem.getData(this)).hasMax() ? data.getMin() + " -> " + data.getMax() : "" + data.getMin())
				: "0";

		lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + format);
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}

	@Override
	public UpgradeInfo loadUpgradeInfo(Object obj) {
		return new DoubleUpgradeInfo(obj);
	}

	@Override
	public void apply(MMOItem mmoitem, UpgradeInfo info) {
		DoubleUpgradeInfo doubleInfo = (DoubleUpgradeInfo) info;

		if (mmoitem.hasData(this)) {
			if (doubleInfo.isRelative())
				((DoubleData) mmoitem.getData(this)).addRelative(doubleInfo.getAmount());
			else
				((DoubleData) mmoitem.getData(this)).add(doubleInfo.getAmount());
		} else
			mmoitem.setData(this, new DoubleData(doubleInfo.getAmount()));
	}

	public class DoubleUpgradeInfo implements UpgradeInfo {
		private final boolean relative;
		private final double amount;

		public DoubleUpgradeInfo(Object obj) {
			Validate.notNull(obj, "Argument must not be null");

			String str = obj.toString();
			if (str.isEmpty())
				throw new IllegalArgumentException("Couldn't read amount");

			relative = str.toCharArray()[str.length() - 1] == '%';
			amount = relative ? Double.parseDouble(str.substring(0, str.length() - 1)) / 100 : Double.parseDouble(str);
		}

		public double getAmount() {
			return amount;
		}

		public boolean isRelative() {
			return relative;
		}
	}
}
