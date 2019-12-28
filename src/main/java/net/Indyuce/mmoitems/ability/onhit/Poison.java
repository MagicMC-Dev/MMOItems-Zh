package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.TargetAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

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
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new TargetAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		LivingEntity target = ((TargetAbilityResult) ability).getTarget();

		target.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1, 0), 32, 1, 1, 1, 0);
		target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1, 0), 24, 1, 1, 1, 0);
		target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 2);
		target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (ability.getModifier("duration") * 20), (int) ability.getModifier("amplifier")));
	}
}
