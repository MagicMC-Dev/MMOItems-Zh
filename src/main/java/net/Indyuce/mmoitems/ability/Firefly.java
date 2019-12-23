package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Firefly extends Ability {
	public Firefly() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("duration", 2.5);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double duration = data.getModifier("duration") * 20;

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration)
					cancel();

				if (stats.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
					stats.getPlayer().setVelocity(stats.getPlayer().getVelocity().multiply(3).setY(1.8));
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, .5f);
					stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
					stats.getPlayer().getWorld().spawnParticle(Particle.CLOUD, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
					cancel();
					return;
				}

				for (Entity entity : stats.getPlayer().getNearbyEntities(1, 1, 1))
					if (MMOUtils.canDamage(stats.getPlayer(), entity)) {
						double damage = data.getModifier("damage");
						double knockback = data.getModifier("knockback");

						stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, .5f);
						stats.getPlayer().getWorld().spawnParticle(Particle.LAVA, stats.getPlayer().getLocation().add(0, 1, 0), 32);
						stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
						stats.getPlayer().getWorld().spawnParticle(Particle.FLAME, stats.getPlayer().getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
						entity.setVelocity(stats.getPlayer().getVelocity().setY(0.3).multiply(1.7 * knockback));
						stats.getPlayer().setVelocity(stats.getPlayer().getEyeLocation().getDirection().multiply(-3).setY(.5));
						new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).damage(stats.getPlayer(), (LivingEntity) entity);
						cancel();
						return;
					}

				Location loc = stats.getPlayer().getLocation().add(0, 1, 0);
				for (double a = 0; a < Math.PI * 2; a += Math.PI / 9) {
					Vector vec = new Vector(.6 * Math.cos(a), .6 * Math.sin(a), 0);
					vec = MMOUtils.rotateFunc(vec, loc);
					loc.add(vec);
					stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 0);
					if (random.nextDouble() < .3)
						stats.getPlayer().getWorld().spawnParticle(Particle.FLAME, loc, 0);
					loc.add(vec.multiply(-1));
				}

				stats.getPlayer().setVelocity(stats.getPlayer().getEyeLocation().getDirection());
				stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
