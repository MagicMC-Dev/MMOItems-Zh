package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;

public class Poison extends Ability {
	public Poison() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 4);
		addModifier("cooldown", 10);
		addModifier("amplifier", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		target = target == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : target;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		target.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1, 0), 32, 1, 1, 1, 0);
		target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1, 0), 24, 1, 1, 1, 0);
		target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 2);
		target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (data.getModifier("duration") * 20), (int) data.getModifier("amplifier")));
	}
}
