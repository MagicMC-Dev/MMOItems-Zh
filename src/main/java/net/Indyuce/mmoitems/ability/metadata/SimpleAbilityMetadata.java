package net.Indyuce.mmoitems.ability.metadata;

import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;

/**
 * @deprecated Abilities were moved over to MythicLib.
 *         AbilityMetadata from MMOItems are now {@link io.lumine.mythic.lib.skill.result.SkillResult}
 */
@Deprecated
public class SimpleAbilityMetadata extends AbilityMetadata {
    private final boolean successful;

    public SimpleAbilityMetadata(AbilityData ability) {
        this(ability, true);
    }

    public SimpleAbilityMetadata(AbilityData ability, boolean successful) {
        super(ability);

        this.successful = successful;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }
}
