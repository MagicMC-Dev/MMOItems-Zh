package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.LocationAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Life_Ender extends Ability {
	public Life_Ender() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 5);
		addModifier("knockback", 1);
		addModifier("radius", 4);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new LocationAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		Location loc = ((LocationAbilityResult) ability).getTarget();
		double damage = ability.getModifier("damage");
		double knockback = ability.getModifier("knockback");
		double radius = ability.getModifier("radius");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 2, 1);
		new BukkitRunnable() {
			int ti = 0;
			Location source = loc.clone().add(5 * Math.cos(random.nextDouble() * 2 * Math.PI), 20, 5 * Math.sin(random.nextDouble() * 2 * Math.PI));
			Vector vec = loc.subtract(source).toVector().multiply((double) 1 / 30);

			public void run() {
				if (ti == 0)
					loc.setDirection(vec);

				for (int k = 0; k < 2; k++) {
					ti++;
					source.add(vec);
					for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
						Vector vec = MMOUtils.rotateFunc(new Vector(Math.cos(i), Math.sin(i), 0), loc);
						source.getWorld().spawnParticle(Particle.SMOKE_LARGE, source, 0, vec.getX(), vec.getY(), vec.getZ(), .1);
					}
				}

				if (ti >= 30) {
					source.getWorld().playSound(source, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);
					source.getWorld().spawnParticle(Particle.FLAME, source, 64, 0, 0, 0, .25);
					source.getWorld().spawnParticle(Particle.LAVA, source, 32);
					for (double j = 0; j < Math.PI * 2; j += Math.PI / 24)
						source.getWorld().spawnParticle(Particle.SMOKE_LARGE, source, 0, Math.cos(j), 0, Math.sin(j), .5);

					for (Entity entity : MMOUtils.getNearbyChunkEntities(source))
						if (entity.getLocation().distanceSquared(source) < radius * radius && MMOUtils.canDamage(stats.getPlayer(), entity)) {
							new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).damage(stats.getPlayer(), (LivingEntity) entity);
							entity.setVelocity(entity.getLocation().subtract(source).toVector().setY(.75).normalize().multiply(knockback));
						}
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
