package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Leap extends Ability {
	public Leap() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("force", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		if (!stats.getPlayer().isOnGround()) {
			result.setSuccessful(false);
			return;
		}

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, stats.getPlayer().getLocation(), 16, 0, 0, 0.1);
		Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(2 * data.getModifier("force"));
		vec.setY(vec.getY() / 2);
		stats.getPlayer().setVelocity(vec);
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti++;
				if (ti > 20)
					cancel();

				stats.getPlayer().getWorld().spawnParticle(Particle.CLOUD, stats.getPlayer().getLocation().add(0, 1, 0), 0);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
