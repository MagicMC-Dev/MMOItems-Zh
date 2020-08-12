package net.Indyuce.mmoitems.ability;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

public class Corrupt extends Ability {
	public Corrupt() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 8);
		addModifier("duration", 4);
		addModifier("amplifier", 1);
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
		double duration = ability.getModifier("duration");
		double amplifier = ability.getModifier("amplifier");
		double radius = 2.7;

		loc.add(0, -1, 0);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, .5f);
		for (double j = 0; j < Math.PI * 2; j += Math.PI / 36) {
			Location loc1 = loc.clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
			double y_max = .5 + random.nextDouble();
			for (double y = 0; y < y_max; y += .1)
				loc1.getWorld().spawnParticle(Particle.REDSTONE, loc1.clone().add(0, y, 0), 1, new Particle.DustOptions(Color.PURPLE, 1));
		}

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= radius * radius) {
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), (LivingEntity) entity);
				((LivingEntity) entity).removePotionEffect(PotionEffectType.WITHER);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (duration * 20), (int) amplifier));
			}
	}
}
