package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class LocationAbility extends Ability<LocationAbilityMetadata> {
    public LocationAbility(CastingMode... allowedModes) {
        super(allowedModes);
    }

    public LocationAbility(String id, String name, CastingMode... allowedModes) {
        super(id, name, allowedModes);
    }

    public LocationAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new LocationAbilityMetadata(ability, attack.getDamager(), target);
    }

    public abstract void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability);
}
