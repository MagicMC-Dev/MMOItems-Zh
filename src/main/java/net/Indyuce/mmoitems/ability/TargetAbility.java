package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class TargetAbility extends Ability<TargetAbilityMetadata> {
    public TargetAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public TargetAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    public TargetAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new TargetAbilityMetadata(ability, attack.getDamager(), target);
    }

    public abstract void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability);
}
