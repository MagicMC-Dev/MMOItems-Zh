package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;

public class RequiredLevelData extends DoubleData {
	public RequiredLevelData(double value) {
		super(value);
	}

	/*
	 * when a gem stone is applied on an item with a lower level requirement,
	 * the item must save the HIGHEST level requirement so that newbies cannot
	 * use over powered items
	 */
	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof RequiredLevelData, "Cannot merge two different stat data types");
		boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-levels", false);

		// Adding up
		if (additiveMerge) {

			// Additive
			setValue(((RequiredLevelData) data).getValue() + getValue());

		} else {

			// Max Level
			setValue(Math.max(((RequiredLevelData) data).getValue(), getValue()));
		}
	}
}
