package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Circular_Slash extends Ability {
	public Circular_Slash() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("radius", 3);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double damage = data.getModifier("damage");
		double radius = data.getModifier("radius");
		double knockback = data.getModifier("knockback");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, .5f);
		stats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));
		for (Entity entity : stats.getPlayer().getNearbyEntities(radius, radius, radius)) {
			if (MMOUtils.canDamage(stats.getPlayer(), entity)) {
				new AttackResult(damage, DamageType.SKILL, DamageType.PHYSICAL).damage(stats.getPlayer(), (LivingEntity) entity);
				Vector v1 = entity.getLocation().toVector();
				Vector v2 = stats.getPlayer().getLocation().toVector();
				double y = .5;
				Vector v3 = v1.subtract(v2).multiply(.5 * knockback).setY(knockback == 0 ? 0 : y);
				entity.setVelocity(v3);
			}
		}
		double step = 12 + (radius * 2.5);
		for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
			Location loc = stats.getPlayer().getLocation().clone();
			loc.add(Math.cos(j) * radius, .75, Math.sin(j) * radius);
			loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 0);
		}
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
	}
}
