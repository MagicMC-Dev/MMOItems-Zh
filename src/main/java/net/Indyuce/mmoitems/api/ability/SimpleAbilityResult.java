package net.Indyuce.mmoitems.api.ability;

import net.Indyuce.mmoitems.stat.data.AbilityData;

public class SimpleAbilityResult extends AbilityResult {
	private final boolean successful;

	public SimpleAbilityResult(AbilityData ability) {
		this(ability, true);
	}

	public SimpleAbilityResult(AbilityData ability, boolean successful) {
		super(ability);

		this.successful = successful;
	}

	@Override
	public boolean isSuccessful() {
		return successful;
	}
}
