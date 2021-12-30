package net.Indyuce.mmoitems.ability.metadata;

import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @deprecated Abilities were moved over to MythicLib.
 *         AbilityMetadata from MMOItems are now {@link io.lumine.mythic.lib.skill.result.SkillResult}
 */
@Deprecated
public class VectorAbilityMetadata extends AbilityMetadata {
    private final Vector target;

    public VectorAbilityMetadata(AbilityData ability, Player caster, LivingEntity target) {
        super(ability);

        this.target = getTargetDirection(caster, target);
    }

    public Vector getTarget() {
        return target;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    public Vector getTargetDirection(Player player, LivingEntity target) {
        return target == null ? player.getEyeLocation().getDirection() : target.getLocation().add(0, target.getHeight() / 2, 0).subtract(player.getLocation().add(0, 1.3, 0)).toVector().normalize();
    }
}
