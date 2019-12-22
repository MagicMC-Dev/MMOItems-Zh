package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;

public class Burn extends Ability {
	public Burn() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 3);
		addModifier("cooldown", 8);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity initialTarget, AbilityData data, ItemAttackResult result) {
		LivingEntity target = initialTarget == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		new BukkitRunnable() {
			final Location loc = target.getLocation();
			double y = 0;

			public void run() {
				for (int j1 = 0; j1 < 3; j1++) {
					y += .04;
					for (int j = 0; j < 2; j++) {
						double xz = y * Math.PI * 1.3 + (j * Math.PI);
						Location loc1 = loc.clone().add(Math.cos(xz), y, Math.sin(xz));
						loc.getWorld().spawnParticle(Particle.FLAME, loc1, 0);
					}
				}
				if (y >= 1.7)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 2);
		target.setFireTicks((int) (target.getFireTicks() + data.getModifier("duration") * 20));
	}
}
