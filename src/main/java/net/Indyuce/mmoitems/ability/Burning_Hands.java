package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;

public class Burning_Hands extends Ability implements Listener {
	public Burning_Hands() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 3);
		addModifier("damage", 2);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double duration = data.getModifier("duration") * 10;
		double damage = data.getModifier("damage") / 2;

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration)
					cancel();

				Location loc = stats.getPlayer().getLocation().add(0, 1.2, 0);
				loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1, 1);

				for (double m = -45; m < 45; m += 5) {
					double a = (m + stats.getPlayer().getEyeLocation().getYaw() + 90) * Math.PI / 180;
					Vector vec = new Vector(Math.cos(a), (random.nextDouble() - .5) * .2, Math.sin(a));
					Location source = loc.clone().add(vec.clone().setY(0));
					source.getWorld().spawnParticle(Particle.FLAME, source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
					if (j % 2 == 0)
						source.getWorld().spawnParticle(Particle.SMOKE_NORMAL, source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
				}

				if (j % 5 == 0)
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (entity.getLocation().distanceSquared(loc) < 60)
							if (stats.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(stats.getPlayer().getLocation().toVector())) < Math.PI / 6)
								if (MMOUtils.canDamage(stats.getPlayer(), entity))
									new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);

			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
