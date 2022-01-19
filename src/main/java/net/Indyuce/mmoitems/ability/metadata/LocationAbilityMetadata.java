package net.Indyuce.mmoitems.ability.metadata;

import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Ability that requires a target location
 *
 * @deprecated Abilities were moved over to MythicLib.
 *         AbilityMetadata from MMOItems are now {@link io.lumine.mythic.lib.skill.result.SkillResult}
 */
@Deprecated
public class LocationAbilityMetadata extends AbilityMetadata {
    private final Location target;

    public LocationAbilityMetadata(AbilityData ability, Player caster, LivingEntity target) {
        super(ability);

        this.target = getTargetLocation(caster, target);
    }

    public Location getTarget() {
        return target;
    }

    @Override
    public boolean isSuccessful() {
        return target != null;
    }

    public Location getTargetLocation(Player player, LivingEntity entity) {
        if (entity != null)
            return entity.getLocation();

        Location loc = player.getTargetBlock(null, 50).getLocation();
        return loc.getBlock().getType() == Material.AIR ? null : loc.add(.5, 1, .5);
    }
}
