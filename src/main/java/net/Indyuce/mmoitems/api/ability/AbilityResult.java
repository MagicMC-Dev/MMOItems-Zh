package net.Indyuce.mmoitems.api.ability;

import net.Indyuce.mmoitems.stat.data.AbilityData;

public abstract class AbilityResult {
	private final AbilityData ability;

	public AbilityResult(AbilityData ability) {
		this.ability = ability;
	}

	public AbilityData getAbility() {
		return ability;
	}

	public double getModifier(String path) {
		return ability.getModifier(path);
	}

	public abstract boolean isSuccessful();
}
