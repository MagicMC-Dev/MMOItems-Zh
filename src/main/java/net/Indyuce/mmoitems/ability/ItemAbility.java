package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.ItemAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class ItemAbility extends Ability<ItemAbilityMetadata> {
    public ItemAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public ItemAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    public ItemAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new ItemAbilityMetadata(ability, attack.getDamager(), target);
    }

    public abstract void whenCast(ItemAttackMetadata attack, ItemAbilityMetadata ability);
}
