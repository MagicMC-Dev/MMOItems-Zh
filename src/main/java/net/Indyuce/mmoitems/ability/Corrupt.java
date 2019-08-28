package net.Indyuce.mmoitems.ability;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
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

public class Corrupt extends Ability {
	public Corrupt() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 8);
		addModifier("duration", 4);
		addModifier("amplifier", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		double damage1 = data.getModifier("damage");
		double duration = data.getModifier("duration");
		double amplifier = data.getModifier("amplifier");
		double radius = 2.7;

		loc.add(0, -1, 0);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, .5f);
		for (double j = 0; j < Math.PI * 2; j += Math.PI / 36) {
			Location loc1 = loc.clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
			double y_max = .5 + random.nextDouble();
			for (double y = 0; y < y_max; y += .1)
				MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc1.clone().add(0, y, 0), Color.PURPLE);
		}

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= radius * radius) {
				MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage1, DamageType.MAGIC);
				((LivingEntity) entity).removePotionEffect(PotionEffectType.WITHER);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (duration * 20), (int) amplifier));
			}
	}
}
