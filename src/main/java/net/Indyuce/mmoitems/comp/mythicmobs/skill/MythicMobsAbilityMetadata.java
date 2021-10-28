package net.Indyuce.mmoitems.comp.mythicmobs.skill;

import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class MythicMobsAbilityMetadata extends AbilityMetadata {
    private final Skill skill;
    private final SkillMetadata skillMeta;

    public MythicMobsAbilityMetadata(AbilityData ability, Skill skill, SkillMetadata skillMeta) {
        super(ability);

        this.skill = skill;
        this.skillMeta = skillMeta;
    }

    public SkillMetadata getSkillMetadata() {
        return skillMeta;
    }

    @Override
    public boolean isSuccessful() {
        return skill.usable(skillMeta, SkillTrigger.CAST);
    }
}