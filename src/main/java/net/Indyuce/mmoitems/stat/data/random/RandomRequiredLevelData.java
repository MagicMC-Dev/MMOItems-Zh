package net.Indyuce.mmoitems.stat.data.random;

import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;
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
	public StatData randomize(GeneratedItemBuilder builder) {
		return new RequiredLevelData(calculate(builder.getLevel()));
	}
}
