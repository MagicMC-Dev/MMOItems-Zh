package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class RestoreData implements StatData, Mergeable {
	private double health, food, saturation;

	public RestoreData(double health, double food, double saturation) {
		this.health = health;
		this.food = food;
		this.saturation = saturation;
	}

	public double getHealth() {
		return health;
	}

	public double getFood() {
		return food;
	}

	public double getSaturation() {
		return saturation;
	}

	public void setHealth(double value) {
		health = value;
	}

	public void setFood(double value) {
		food = value;
	}

	public void setSaturation(double value) {
		saturation = value;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof RestoreData, "Cannot merge two different stat data types");
		health += ((RestoreData) data).health;
		food += ((RestoreData) data).food;
		saturation += ((RestoreData) data).saturation;
	}

	@Override
	public @NotNull StatData cloneData() {
		return new RestoreData(getHealth(), getFood(), getSaturation());
	}
}