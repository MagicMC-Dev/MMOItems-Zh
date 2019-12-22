package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Shock extends Ability {
	public Shock() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 2);
		addModifier("cooldown", 8);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity initialTarget, AbilityData data, AttackResult result) {
		LivingEntity target = initialTarget == null ? MMOItems.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		double duration = data.getModifier("duration");

		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ZOMBIE_PIGMAN_ANGRY.toSound(), 1, 2);
		new BukkitRunnable() {
			final Location loc = target.getLocation();
			double rads = Math.toRadians(stats.getPlayer().getEyeLocation().getYaw() - 90);
			double ti = rads;

			public void run() {
				for (int j = 0; j < 3; j++) {
					ti += Math.PI / 15;
					target.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(Math.cos(ti), 1, Math.sin(ti)), 0);
				}
				if (ti >= Math.PI * 2 + rads)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti++;
				if (ti > (duration > 30 ? 30 : duration) * 10)
					cancel();
				if (!target.isDead())
					target.playEffect(EntityEffect.HURT);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
