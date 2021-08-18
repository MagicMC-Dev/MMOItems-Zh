package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Tactical_Grenade extends TargetAbility {
    public Tactical_Grenade() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
        addModifier("knock-up", 1);
        addModifier("damage", 4);
        addModifier("radius", 4);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        new BukkitRunnable() {
            final Location loc = attack.getDamager().getLocation().add(0, .1, 0);
            final double radius = ability.getModifier("radius");
            final double knockup = .7 * ability.getModifier("knock-up");
            final List<Integer> hit = new ArrayList<>();
            int j = 0;

            public void run() {
                j++;
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
                    cancel();
                    return;
                }

                Vector vec = target.getLocation().add(0, .1, 0).subtract(loc).toVector();
                vec = vec.length() < 3 ? vec : vec.normalize().multiply(3);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 32, 1, 0, 1, 0);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 16, 1, 0, 1, .05);
                loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2, 0);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

                for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                    if (!hit.contains(entity.getEntityId()) && MMOUtils.canDamage(attack.getDamager(), entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {

                        /*
                         * stop the runnable as soon as the grenade finally hits
                         * the initial target.
                         */
                        hit.add(entity.getEntityId());
                        if (entity.equals(target))
                            cancel();

                        new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
                        entity.setVelocity(entity.getVelocity().add(offsetVector(knockup)));
                    }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 12);
	}

	private Vector offsetVector(double y) {
		return new Vector(2 * (random.nextDouble() - .5), y, 2 * (random.nextDouble() - .5));
	}
}