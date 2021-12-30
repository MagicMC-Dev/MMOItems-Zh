package net.Indyuce.mmoitems.ability.metadata;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;

@Deprecated
public class BackwardsCompatibleAbilityMetadata extends AbilityMetadata {
    private final SkillMetadata skillMeta;
    private final SkillResult result;

    public BackwardsCompatibleAbilityMetadata(SkillMetadata skillMeta, SkillResult result, AbilityData abilityData) {
        super(abilityData);

        this.skillMeta = skillMeta;
        this.result = result;
    }

    @Override
    public boolean isSuccessful() {
        return result.isSuccessful(skillMeta);
    }

    public SkillMetadata getMetadata() {
        return skillMeta;
    }

    public SkillResult getResult() {
        return result;
    }
}