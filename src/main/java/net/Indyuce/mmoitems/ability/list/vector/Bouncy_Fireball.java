package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Bouncy_Fireball extends VectorAbility {
    public Bouncy_Fireball() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 20);
        addModifier("damage", 5);
        addModifier("ignite", 40);
        addModifier("speed", 1);
        addModifier("radius", 4);
        addModifier("mana", 0);
        addModifier("stamina", 0);
	}

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 2, 0);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(0).normalize().multiply(.5 * ability.getModifier("speed"));
            final Location loc = attack.getDamager().getLocation().clone().add(0, 1.2, 0);
            int j = 0;
            int bounces = 0;

            double y = .3;

            public void run() {
                if (j++ > 100) {
                    loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 32, 0, 0, 0, .05);
                    loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
                    cancel();
                    return;
                }

                loc.add(vec);
                loc.add(0, y, 0);
                if (y > -.6)
                    y -= .05;

				loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0, 0, 0, .03);
				loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, .03);

				if (loc.getBlock().getType().isSolid()) {
					loc.add(0, -y, 0);
					loc.add(vec.clone().multiply(-1));
					y = .4;
					bounces++;
					loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 3, 2);
				}

				if (bounces > 2) {
					double radius = ability.getModifier("radius");
					double damage = ability.getModifier("damage");
					double ignite = ability.getModifier("ignite");

					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < radius * radius)
                            if (MMOUtils.canDamage(attack.getDamager(), entity)) {
                                new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
                                entity.setFireTicks((int) (ignite * 20));
                            }

					loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 12, 2, 2, 2, 0);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 48, 0, 0, 0, .2);
					loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, 0);
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
