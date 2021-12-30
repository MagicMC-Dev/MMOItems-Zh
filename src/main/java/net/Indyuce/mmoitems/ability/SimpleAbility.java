package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

/**
 * @deprecated Abilities were moved over to MythicLib.
 *         Abilities are being replaced by {@link io.lumine.mythic.lib.skill.handler.SkillHandler}
 */
@Deprecated
public abstract class SimpleAbility extends Ability<SimpleAbilityMetadata> {
    public SimpleAbility() {
        super();
    }

    public SimpleAbility(String id, String name) {
        super(id, name);
    }

    @Override
    public SimpleAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new SimpleAbilityMetadata(ability);
    }
}
