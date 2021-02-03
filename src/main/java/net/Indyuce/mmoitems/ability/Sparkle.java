package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.TargetAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.version.VersionSound;

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
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new TargetAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		LivingEntity target = ((TargetAbilityResult) ability).getTarget();
		double damage = ability.getModifier("damage");
		double radius = ability.getModifier("radius");
		double limit = ability.getModifier("limit");

		new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), target);
		target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);

		int count = 0;
		for (Entity entity : target.getNearbyEntities(radius, radius, radius))
			if (count < limit && entity instanceof LivingEntity && entity != stats.getPlayer() && !(entity instanceof ArmorStand)) {
				count++;
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), (LivingEntity) entity);
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
