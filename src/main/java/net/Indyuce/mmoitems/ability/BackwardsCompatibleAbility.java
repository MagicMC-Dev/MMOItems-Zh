package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.custom.variable.VariableList;
import io.lumine.mythic.lib.skill.custom.variable.VariableScope;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmoitems.ability.metadata.BackwardsCompatibleAbilityMetadata;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class BackwardsCompatibleAbility extends Ability<BackwardsCompatibleAbilityMetadata> {
    private final RegisteredSkill registeredSkill;

    public BackwardsCompatibleAbility(RegisteredSkill registeredSkill) {
        super(registeredSkill.getHandler().getId(), registeredSkill.getName());

        this.registeredSkill = registeredSkill;
    }

    @Nullable
    @Override
    public BackwardsCompatibleAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        SkillMetadata skillMeta = new SkillMetadata(ability, attack, new VariableList(VariableScope.SKILL), attack, attack.getPlayer().getLocation(), null, target, null);
        return new BackwardsCompatibleAbilityMetadata(skillMeta, registeredSkill.getHandler().getResult(skillMeta), ability);
    }

    @Override
    public void whenCast(AttackMetadata attack, BackwardsCompatibleAbilityMetadata ability) {
        ((SkillHandler) registeredSkill.getHandler()).whenCast(ability.getResult(), ability.getMetadata());
    }
}
