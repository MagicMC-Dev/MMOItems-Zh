package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
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
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class DoubleStat extends ItemStat implements Upgradable {
	private static final DecimalFormat digit = new DecimalFormat("0.####");

	public DoubleStat(String id, Material mat, String name, String[] lore) {
		super(id, mat, name, lore, new String[] { "!miscellaneous", "!block", "all" });
	}

	public DoubleStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
		super(id, mat, name, lore, types, materials);
	}

	/**
	 * @return If this stat supports negatives stat values
	 */
	public boolean handleNegativeStats() {
		return true;
	}

	@Override
	public RandomStatData whenInitialized(Object object) {

		if (object instanceof Number)
			return new NumericStatFormula(Double.parseDouble(object.toString()), 0, 0, 0);

		if (object instanceof ConfigurationSection)
			return new NumericStatFormula(object);

		throw new IllegalArgumentException("Must specify a number or a config section");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		if (value < 0 && !handleNegativeStats())
			return;

		// If the value is 0 the lore will not be applied
		// but the stat will still be added to the nbt
		if (value != 0)
			item.getLore().insert(getPath(), formatNumericStat(value, "#", new StatFormat("##").format(value)));

		item.addItemTag(new ItemTag(getNBTPath(), value));
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
				"Second Format: {Base} {Scaling Value} {Spread} {Max Spread}", "Third Format: {Min Value} -> {Max Value}");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		double base, scale, spread, maxSpread;

		/**
		 * Supports the old RANGE formula with a minimum and a maximum value and
		 * automatically makes the conversion to the newest system. This way
		 * users can keep using the old system if they don't want to adapt to
		 * the complex gaussian stat calculation
		 */
		if (message.contains("->")) {
			String[] split = message.replace(" ", "").split(Pattern.quote("->"));
			Validate.isTrue(split.length > 1, "You must specif two (both min and max) values");

			double min = Double.parseDouble(split[0]), max = Double.parseDouble(split[1]);
			Validate.isTrue(max > min, "Max value must be greater than min value");

			base = MMOUtils.truncation(min == -max ? (max - min) * .05 : (min + max) / 2, 3);
			scale = 0; // No scale
			maxSpread = MMOUtils.truncation((max - min) / (2 * base), 3);
			spread = MMOUtils.truncation(.8 * maxSpread, 3);
		}

		/**
		 * Newest system with gaussian values calculation
		 */
		else {
			String[] split = message.split(" ");
			base = MMOUtils.parseDouble(split[0]);
			scale = split.length > 1 ? MMOUtils.parseDouble(split[1]) : 0;
			spread = split.length > 2 ? MMOUtils.parseDouble(split[2]) : 0;
			maxSpread = split.length > 3 ? MMOUtils.parseDouble(split[3]) : 0;
		}

		// Save as a flat formula
		if (scale == 0 && spread == 0 && maxSpread == 0)
			inv.getEditedSection().set(getPath(), base);

		else {
			inv.getEditedSection().set(getPath() + ".base", base);
			inv.getEditedSection().set(getPath() + ".scale", scale == 0 ? null : scale);
			inv.getEditedSection().set(getPath() + ".spread", spread == 0 ? null : spread);
			inv.getEditedSection().set(getPath() + ".max-spread", maxSpread == 0 ? null : maxSpread);
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
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
		if (statData.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) statData.get();
			lore.add(ChatColor.GRAY + "Base Value: " + ChatColor.GREEN + digit.format(data.getBase())
					+ (data.getScale() != 0 ? ChatColor.GRAY + " (+" + ChatColor.GREEN + digit.format(data.getScale()) + ChatColor.GRAY + ")" : ""));
			if (data.getSpread() > 0)
				lore.add(ChatColor.GRAY + "Spread: " + ChatColor.GREEN + digit.format(data.getSpread() * 100) + "%" + ChatColor.GRAY + " (Max: "
						+ ChatColor.GREEN + digit.format(data.getMaxSpread() * 100) + "%" + ChatColor.GRAY + ")");

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

	public static class DoubleUpgradeInfo implements UpgradeInfo {
		private final boolean relative;
		private final double amount;

		public DoubleUpgradeInfo(Object obj) {
			Validate.notNull(obj, "Argument must not be null");

			String str = obj.toString();
			if (str.isEmpty())
				throw new IllegalArgumentException("Couldn't read amount");

			relative = str.toCharArray()[str.length() - 1] == '%';
			amount = relative ? MMOUtils.parseDouble(str.substring(0, str.length() - 1)) / 100 : MMOUtils.parseDouble(str);
		}

		public double getAmount() {
			return amount;
		}

		public boolean isRelative() {
			return relative;
		}
	}
}
