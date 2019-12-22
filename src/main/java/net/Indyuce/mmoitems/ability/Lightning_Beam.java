package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

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
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}
		
		loc = getFirstNonSolidBlock(loc);

		double damage = data.getModifier("damage");
		double radius = data.getModifier("radius");

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= radius * radius)
				new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);

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
