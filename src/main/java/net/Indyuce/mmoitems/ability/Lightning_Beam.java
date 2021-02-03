package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.LocationAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.version.VersionSound;

public class Lightning_Beam extends Ability {
	public Lightning_Beam() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 8);
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
		final Location loc = getFirstNonSolidBlock(((LocationAbilityResult) ability).getTarget());
		double damage = ability.getModifier("damage");
		double radius = ability.getModifier("radius");

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= radius * radius)
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), (LivingEntity) entity);

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 0);
		loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 64, 0, 0, 0, .2);
		loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 32, 0, 0, 0, .2);
		Vector vec = new Vector(0, .3, 0);
		for (double j = 0; j < 40; j += .3)
			loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.add(vec), 6, .1, .1, .1, .01);
	}

	private Location getFirstNonSolidBlock(Location loc) {
		Location initial = loc.clone();
		for (int j = 0; j < 5; j++)
			if (!loc.add(0, 1, 0).getBlock().getType().isSolid())
				return loc;
		return initial;
	}
}
