package net.Indyuce.mmoitems.comp.rpg.damage;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;

public class DamageInfo {
	private final double damage;
	private final List<DamageType> damageTypes;

	public DamageInfo(double damage, DamageType... damageTypes) {
		this(damage, Arrays.asList(damageTypes));
	}

	public DamageInfo(double damage, List<DamageType> damageTypes) {
		this.damage = damage;
		this.damageTypes = damageTypes;
	}

	public double getValue() {
		return damage;
	}

	public List<DamageType> getTypes() {
		return damageTypes;
	}

	public boolean hasType(DamageType type) {
		return damageTypes.contains(type);
	}

	public AttackResult toAttackResult() {
		return new AttackResult(true, damage, damageTypes);
	}
}
