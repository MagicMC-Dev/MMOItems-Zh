package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.data.upgrade.UpgradeInfo;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class DoubleStat extends ItemStat implements Upgradable {
	public DoubleStat(ItemStack item, String name, String[] lore, String path) {
		super(item, name, lore, path, new String[] { "!miscellaneous", "all" });
	}

	public DoubleStat(ItemStack item, String name, String[] lore, String path, String[] types, Material... materials) {
		super(item, name, lore, path, types, materials);
	}

	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(this, new DoubleData(config.getString(getPath())));
		return true;
	}

	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag(getNBTPath(), value));
		item.getLore().insert(getPath(), format(value, "#", new StatFormat("##").format(value)));
		return true;
	}

	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = inv.getItemType().getConfigFile();
			config.getConfig().set(inv.getItemId() + "." + getPath(), null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ChatColor.GRAY + ".");
			return true;
		}
		new StatEdition(inv, this).enable("Write in the chat the numeric value you want.", "Or write [MIN-VALUE]=[MAX-VALUE] to make the stat random.");
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
		config.getConfig().set(inv.getItemId() + "." + getPath(), split.length > 1 ? value + "=" + value1 : value);
		if (value == 0 && value1 == 0)
			config.getConfig().set(inv.getItemId() + "." + getPath(), null);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + (value1 != 0 ? "{between " + value + " and " + value1 + "}" : "" + value) + ".");
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			mmoitem.setData(this, new DoubleData(item.getDouble(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");

		String[] split = config.contains(id + "." + getPath()) ? config.getString(id + "." + getPath()).split("\\=") : new String[] { "0" };
		String format = split.length > 1 ? tryParse(split[0]) + " -> " + tryParse(split[1]) : "" + config.getDouble(id + "." + getPath());

		lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + format);
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}

	private double tryParse(String input) {
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException exception) {
			return 0;
		}
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

	public class DoubleData extends StatData {
		private double min, max;

		public DoubleData() {
		}

		public DoubleData(String string) {
			String[] split = string.split("\\=");
			if (split.length == 2) {
				min = Double.parseDouble(split[0]);
				max = Double.parseDouble(split[1]);
			} else
				min = Double.valueOf(string);
		}

		public DoubleData(double value) {
			this(value, 0);
		}

		private DoubleData(double min, double max) {
			this.min = min;
			this.max = max;
		}

		public boolean hasMax() {
			return max > min;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

		public void setMax(double value) {
			max = value;
		}

		public void setMin(double value) {
			min = value;
		}

		public void add(double value) {
			min = min + value;
		}

		public void addRelative(double value) {
			min *= 1 + value;
		}

		public double generateNewValue() {
			return hasMax() ? min + random.nextDouble() * (max - min) : min;
		}
	}
}
