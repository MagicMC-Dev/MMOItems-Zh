package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;

/**
 * @deprecated Abilities were moved over to MythicLib.
 *         AbilityMetadata from MMOItems are now {@link io.lumine.mythic.lib.skill.result.SkillResult}
 */
@Deprecated
public abstract class AbilityMetadata implements SkillResult {
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

	@Override
	public boolean isSuccessful(SkillMetadata skillMetadata) {
		return isSuccessful();
	}
}
