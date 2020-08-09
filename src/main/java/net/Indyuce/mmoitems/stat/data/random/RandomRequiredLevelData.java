package net.Indyuce.mmoitems.stat.data.random;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.RequiredLevelData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomRequiredLevelData extends NumericStatFormula {
	public RandomRequiredLevelData(Object object) {
		super(object);
	}

	public RandomRequiredLevelData(double base, double scale, double spread, double maxSpread) {
		super(base, scale, spread, maxSpread);
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new RequiredLevelData(calculate(builder.getLevel()));
	}
}
