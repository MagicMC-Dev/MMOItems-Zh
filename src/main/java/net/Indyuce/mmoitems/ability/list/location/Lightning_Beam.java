package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Lightning_Beam extends LocationAbility {
	public Lightning_Beam() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 8);
		addModifier("radius", 5);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability) {
		final Location loc = getFirstNonSolidBlock(ability.getTarget());
		double damage = ability.getModifier("damage");
		double radius = ability.getModifier("radius");

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (MMOUtils.canTarget(attack.getDamager(), entity) && entity.getLocation().distanceSquared(loc) <= radius * radius)
				new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);

		attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 0);
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
