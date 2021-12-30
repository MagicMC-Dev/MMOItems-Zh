package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

/**
 * @deprecated Abilities were moved over to MythicLib.
 *         Abilities are being replaced by {@link io.lumine.mythic.lib.skill.handler.SkillHandler}
 */
@Deprecated
public abstract class LocationAbility extends Ability<LocationAbilityMetadata> {
    public LocationAbility() {
        super();
    }

    public LocationAbility(String id, String name) {
        super(id, name);
    }

    public LocationAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new LocationAbilityMetadata(ability, attack.getPlayer(), target);
    }
}
