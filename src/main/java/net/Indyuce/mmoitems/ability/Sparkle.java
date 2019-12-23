package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Sparkle extends Ability {
	public Sparkle() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("damage", 4);
		addModifier("limit", 5);
		addModifier("radius", 6);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		target = target == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : target;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		double damage = data.getModifier("damage");
		double radius = data.getModifier("radius");
		double limit = data.getModifier("limit");

		new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, target);
		target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);

		int count = 0;
		for (Entity entity : target.getNearbyEntities(radius, radius, radius))
			if (count < limit && entity instanceof LivingEntity && entity != stats.getPlayer() && !(entity instanceof ArmorStand)) {
				count++;
				new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
				entity.getWorld().playSound(entity.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);
				Location loc_t = target.getLocation().add(0, .75, 0);
				Location loc_ent = entity.getLocation().add(0, .75, 0);
				for (double j1 = 0; j1 < 1; j1 += .04) {
					Vector d = loc_ent.toVector().subtract(loc_t.toVector());
					target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc_t.clone().add(d.multiply(j1)), 3, .1, .1, .1, .008);
				}
			}
	}
}
