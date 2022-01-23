package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

/**
 * Ability that requires a direction to be cast. For
 * instance, a projectile like {@link Firebolt}
 *
 * @deprecated Abilities were moved over to MythicLib.
 *         Abilities are being replaced by {@link io.lumine.mythic.lib.skill.handler.SkillHandler}
 */
@Deprecated
public abstract class VectorAbility extends Ability<VectorAbilityMetadata> {
    public VectorAbility() {
        super();
    }

    public VectorAbility(String id, String name) {
        super(id, name);
    }

    public VectorAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new VectorAbilityMetadata(ability, attack.getPlayer(), target);
    }
}
