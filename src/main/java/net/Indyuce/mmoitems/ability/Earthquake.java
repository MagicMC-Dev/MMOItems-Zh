package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;

public class Earthquake extends Ability {
	public Earthquake() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 3);
		addModifier("duration", 2);
		addModifier("amplifier", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		if (!stats.getPlayer().isOnGround()) {
			result.setSuccessful(false);
			return;
		}

		double damage = data.getModifier("damage");
		double slowDuration = data.getModifier("duration");
		double slowAmplifier = data.getModifier("amplifier");

		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).setY(0);
			Location loc = stats.getPlayer().getLocation();
			int ti = 0;
			List<Integer> hit = new ArrayList<>();

			public void run() {
				ti++;
				if (ti > 20)
					cancel();

				loc.add(vec);
				loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .5, 0, .5, 0);
				loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 2, 1);

				for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
					if (MMOUtils.canDamage(stats.getPlayer(), entity) && loc.distanceSquared(entity.getLocation()) < 2 && !hit.contains(entity.getEntityId())) {
						hit.add(entity.getEntityId());
						new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
