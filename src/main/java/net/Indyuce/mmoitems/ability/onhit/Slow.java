package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;

public class Slow extends Ability {
	public Slow() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 5);
		addModifier("duration", 3);
		addModifier("amplifier", 1);
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
			double ti = 0;

			public void run() {
				ti += Math.PI / 10;
				if (ti >= Math.PI * 2)
					cancel();

				for (double j = 0; j < Math.PI * 2; j += Math.PI)
					for (double r = 0; r < .7; r += .1)
						MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos((ti / 2) + j + (Math.PI * r)) * r * 2, .1, Math.sin((ti / 2) + j + (Math.PI * r)) * r * 2), Color.WHITE);

			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LLAMA_ANGRY, 1, 2);
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (data.getModifier("duration") * 20), (int) data.getModifier("amplifier")));
	}
}
