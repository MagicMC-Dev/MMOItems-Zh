package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Vampirism extends Ability {
	public Vampirism() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 8);
		addModifier("drain", 10);
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

		new BukkitRunnable() {
			double ti = 0;
			final Location loc = target.getLocation();
			double dis = 0;

			public void run() {
				for (int j1 = 0; j1 < 4; j1++) {
					ti += .75;
					dis += ti <= 10 ? .15 : -.15;

					for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
						MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(j + (ti / 20)) * dis, 0, Math.sin(j + (ti / 20)) * dis), Color.RED);
				}
				if (ti >= 17)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_DRINK, 1, 2);
		MMOUtils.heal(stats.getPlayer(), result.getDamage() * data.getModifier("drain") / 100);
	}
}
