package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class PickaxePower extends DoubleStat {
	public PickaxePower() {
		super("PICKAXE_POWER", Material.IRON_PICKAXE, "Pickaxe Power",
				new String[] { "The breaking strength of the", "item when mining custom blocks." }, new String[] { "tool" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
		int pickPower = (int) data.getValue();

		item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", pickPower));
		item.getLore().insert("pickaxe-power", formatNumericStat(pickPower, "{value}", String.valueOf(pickPower)));
	}
	@Override
	public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
		Validate.isTrue(currentData instanceof DoubleData, "Current Data is not Double Data");
		Validate.isTrue(templateData instanceof NumericStatFormula, "Template Data is not Numeric Stat Formula");

		// Get Value
		double techMinimum = ((NumericStatFormula) templateData).calculate(0, -2.5);
		double techMaximum = ((NumericStatFormula) templateData).calculate(0, 2.5);

		// Cancel if it its NEGATIVE and this doesn't support negative stats.
		if (techMaximum < 0 && !handleNegativeStats()) {
			return;
		}
		if (techMinimum < 0 && !handleNegativeStats()) {
			techMinimum = 0;
		}
		if (techMinimum < ((NumericStatFormula) templateData).getBase() - ((NumericStatFormula) templateData).getMaxSpread()) {
			techMinimum = ((NumericStatFormula) templateData).getBase() - ((NumericStatFormula) templateData).getMaxSpread();
		}
		if (techMaximum > ((NumericStatFormula) templateData).getBase() + ((NumericStatFormula) templateData).getMaxSpread()) {
			techMaximum = ((NumericStatFormula) templateData).getBase() + ((NumericStatFormula) templateData).getMaxSpread();
		}

		// Add NBT Path
		item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", ((DoubleData) currentData).getValue()));

		// Display if not ZERO
		if (techMinimum != 0 || techMaximum != 0) {

			String builtRange;
			if (SilentNumbers.round(techMinimum, 2) == SilentNumbers.round(techMaximum, 2)) { builtRange = MythicLib.plugin.getMMOConfig().decimals.format(techMinimum); }
			else { builtRange = MythicLib.plugin.getMMOConfig().decimals.format(techMinimum) + "-" + MythicLib.plugin.getMMOConfig().decimals.format(techMaximum); }

			// Just display normally
			item.getLore().insert("pickaxe-power", formatNumericStat(techMinimum, "{value}", builtRange));
		}
	}
}
