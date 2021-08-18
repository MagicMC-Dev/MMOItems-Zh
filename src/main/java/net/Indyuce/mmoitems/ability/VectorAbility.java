package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.list.vector.Firebolt;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

/**
 * Ability that requires a direction to be cast. For
 * instance, a projectile like {@link Firebolt}
 */
public abstract class VectorAbility extends Ability<VectorAbilityMetadata> {
    public VectorAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public VectorAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    public VectorAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new VectorAbilityMetadata(ability, attack.getDamager(), target);
    }

    public abstract void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability);
}
