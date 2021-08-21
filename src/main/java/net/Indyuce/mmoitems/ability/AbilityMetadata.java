package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.stat.data.AbilityData;

public abstract class AbilityMetadata {
    private final AbilityData ability;

    public AbilityMetadata(AbilityData ability) {
        this.ability = ability;
    }

    public AbilityData getAbility() {
        return ability;
    }

    /**
     * @param path
     *            Path of ability modifier
	 * @return Calculates a new value for a given ability modifier
	 */
	public double getModifier(String path) {
		return ability.getModifier(path);
	}

	/**
	 * @return If the ability is cast successfully. This method is used to apply
	 *         extra ability conditions (player must be on the ground, must aim
	 *         at an entity..)
	 */
	public abstract boolean isSuccessful();
}
