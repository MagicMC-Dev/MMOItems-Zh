package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.StatData;

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
		setValue(Math.max(getValue(), ((DoubleData) data).getValue()));
	}
}
