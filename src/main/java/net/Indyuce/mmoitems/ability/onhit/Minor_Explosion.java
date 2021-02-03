package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.LocationAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;

public class Minor_Explosion extends Ability {
	public Minor_Explosion() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("damage", 6);
		addModifier("knockback", 1);
		addModifier("radius", 5);
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
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);
		double knockback = ability.getModifier("knockback");

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 32, 1.7, 1.7, 1.7, 0);
		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 64, 0, 0, 0, .3);
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity)) {
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), (LivingEntity) entity);
				entity.setVelocity(MMOUtils.normalize(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.2).multiply(2 * knockback));
			}
	}
}
