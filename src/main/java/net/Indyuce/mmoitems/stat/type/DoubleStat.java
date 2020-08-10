package net.Indyuce.mmoitems.stat.type;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class DoubleStat extends ItemStat implements Upgradable {
	private static final DecimalFormat digit = new DecimalFormat("0.####");

	public DoubleStat(String id, ItemStack item, String name, String[] lore) {
		super(id, item, name, lore, new String[] { "!miscellaneous", "!block", "all" });
	}

	public DoubleStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {

		if (object instanceof Number)
			return new NumericStatFormula(Double.valueOf(object.toString()), 0, 0, 0);

		if (object instanceof ConfigurationSection)
			return new NumericStatFormula((ConfigurationSection) object);

		throw new IllegalArgumentException("Must specify a number or a config section");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag(getNBTPath(), value));
		if (value > 0)
			item.getLore().insert(getPath(), formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set(getPath(), null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ChatColor.GRAY + ".");
			return;
		}
		new StatEdition(inv, this).enable("Write in the chat the numeric value you want.",
				"Second Format: {Base} {Scaling-Value} {Spread} {Max-Spread}");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String[] split = message.split("\\ ");
		double base = Double.parseDouble(split[0]);
		double scale = split.length > 1 ? Double.parseDouble(split[1]) : 0;
		double spread = split.length > 2 ? Double.parseDouble(split[2]) : 0;
		double maxSpread = split.length > 3 ? Double.parseDouble(split[3]) : 0;

		// save as number
		if (scale == 0 && spread == 0 && maxSpread == 0)
			inv.getEditedSection().set(getPath(), base);

		else {
			inv.getEditedSection().set(getPath() + ".base", base);
			inv.getEditedSection().set(getPath() + ".scale", scale == 0 ? null : scale);
			inv.getEditedSection().set(getPath() + ".spread", spread == 0 ? null : spread);
			inv.getEditedSection().set(getPath() + ".maxSpread", maxSpread == 0 ? null : maxSpread);
		}

		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to {" + base + " - " + scale + " - " + spread
				+ " - " + maxSpread + "}");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getDouble(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) optional.get();
			lore.add(ChatColor.GRAY + "Base Value: " + ChatColor.RED + digit.format(data.getBase())
					+ (data.getScale() != 0 ? ChatColor.GRAY + " (+" + ChatColor.RED + digit.format(data.getScale()) + ChatColor.GRAY + ")" : ""));
			if (data.getSpread() > 0)
				lore.add(ChatColor.GRAY + "Spread: " + ChatColor.RED + digit.format(data.getSpread() * 100) + "%" + ChatColor.GRAY + " (Max: "
						+ ChatColor.RED + digit.format(data.getMaxSpread() * 100) + "%" + ChatColor.GRAY + ")");

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + "---");

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
