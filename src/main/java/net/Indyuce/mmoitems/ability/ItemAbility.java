package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ability.metadata.ItemAbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

/**
 * @deprecated Abilities were moved over to MythicLib.
 * Abilities are being replaced by {@link io.lumine.mythic.lib.skill.handler.SkillHandler}
 */
@Deprecated
public abstract class ItemAbility extends Ability<ItemAbilityMetadata> {
    public ItemAbility() {
        super();
    }

    public ItemAbility(String id, String name) {
        super(id, name);
    }

    public ItemAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return new ItemAbilityMetadata(ability, attack.getPlayer(), target);
    }
}
