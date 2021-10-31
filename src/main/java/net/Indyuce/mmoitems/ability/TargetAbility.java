package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class TargetAbility extends Ability<TargetAbilityMetadata> {
    public TargetAbility() {
        super();
    }

    public TargetAbility(String id, String name) {
        super(id, name);
    }

    public TargetAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new TargetAbilityMetadata(ability, attack.getPlayer(), target);
    }
}
