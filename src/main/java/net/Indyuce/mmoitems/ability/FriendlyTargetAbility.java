package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ability.metadata.FriendlyTargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public abstract class FriendlyTargetAbility extends Ability<FriendlyTargetAbilityMetadata> {
    public FriendlyTargetAbility() {
        super();
    }

    public FriendlyTargetAbility(String id, String name) {
        super(id, name);
    }

    public FriendlyTargetAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new FriendlyTargetAbilityMetadata(ability, attack.getPlayer(), target);
    }

    public abstract void whenCast(AttackMetadata attack, FriendlyTargetAbilityMetadata ability);
}
