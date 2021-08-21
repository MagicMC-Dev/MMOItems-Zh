package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class SimpleAbility extends Ability<SimpleAbilityMetadata> {
    public SimpleAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public SimpleAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    @Override
    public SimpleAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new SimpleAbilityMetadata(ability);
    }

    @Override
    public abstract void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability);
}
