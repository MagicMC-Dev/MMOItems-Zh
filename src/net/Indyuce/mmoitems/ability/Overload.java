package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Overload extends Ability {
	public Overload() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("cooldown", 10);
		addModifier("radius", 6);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double damage1 = data.getModifier("damage");
		double radius = data.getModifier("radius");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 0);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 0);
		stats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));

		for (Entity entity : stats.getPlayer().getNearbyEntities(radius, radius, radius))
			if (MMOUtils.canDamage(stats.getPlayer(), entity))
				MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage1, DamageType.MAGIC);

		double step = 12 + (radius * 2.5);
		for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
			Location loc = stats.getPlayer().getLocation().clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
			stats.getPlayer().getWorld().spawnParticle(Particle.CLOUD, loc, 4, 0, 0, 0, .05);
			stats.getPlayer().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 4, 0, 0, 0, .05);
		}
	}
}
