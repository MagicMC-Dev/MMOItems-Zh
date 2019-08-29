package net.Indyuce.mmoitems.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class DamageInfo {
	private final Set<DamageType> types;
	private final double value;

	public DamageInfo(DamageType... type) {
		this(0, type);
	}

	public DamageInfo(double value, DamageType... types) {
		Validate.notEmpty(types, "Damage must have at least one damage type!");

		this.types = new HashSet<>(Arrays.asList(types));
		this.value = value;
	}

	public DamageInfo merge(DamageInfo info) {
		types.addAll(info.getTypes());
		return this;
	}

	public Set<DamageType> getTypes() {
		return types;
	}

	public boolean hasType(DamageType type) {
		return types.contains(type);
	}

	public double getValue() {
		return value;
	}

	public enum DamageType {

		/*
		 * skills or abilities dealing magic damage
		 */
		MAGICAL,

		/*
		 * skills or abilities dealing physical damage
		 */
		PHYSICAL,

		/*
		 * weapons dealing damage
		 */
		WEAPON,

		/*
		 * skill damage
		 */
		SKILL,

		/*
		 * projectile based weapons or skills
		 */
		PROJECTILE;
	}
}
