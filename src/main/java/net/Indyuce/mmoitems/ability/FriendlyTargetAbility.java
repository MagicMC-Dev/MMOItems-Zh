package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.FriendlyTargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class FriendlyTargetAbility extends Ability<FriendlyTargetAbilityMetadata> {
    public FriendlyTargetAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public FriendlyTargetAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    public FriendlyTargetAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new FriendlyTargetAbilityMetadata(ability, attack.getDamager(), target);
    }

    public abstract void whenCast(ItemAttackMetadata attack, FriendlyTargetAbilityMetadata ability);
}
