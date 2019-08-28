package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Confuse extends Ability {
	public Confuse() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 7);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity initialTarget, AbilityData data, AttackResult result) {
		LivingEntity target = initialTarget == null ? MMOItems.plugin.getVersion().getVersionWrapper().rayTrace(stats.getPlayer(), 50).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHEEP_DEATH, 1, 2);
		new BukkitRunnable() {
			final Location loc = target.getLocation();
			double rads = Math.toRadians(stats.getPlayer().getEyeLocation().getYaw() - 90);
			double ti = rads;

			public void run() {
				for (int j1 = 0; j1 < 3; j1++) {
					ti += Math.PI / 15;
					Location loc1 = loc.clone().add(Math.cos(ti), 1, Math.sin(ti));
					loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1, 0);
				}
				if (ti >= Math.PI * 2 + rads)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		Location loc = target.getLocation().clone();
		loc.setYaw(target.getLocation().getYaw() - 180);
		target.teleport(loc);
	}
}
