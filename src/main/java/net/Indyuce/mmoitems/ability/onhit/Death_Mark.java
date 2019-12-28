package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.TargetAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Death_Mark extends Ability {
	public Death_Mark() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 7);
		addModifier("damage", 5);
		addModifier("duration", 3);
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

		double duration = ability.getModifier("duration") * 20;
		double dps = ability.getModifier("damage") / duration * 20;

		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti++;
				if (ti > duration || target == null || target.isDead()) {
					cancel();
					return;
				}

				target.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation(), 4, .2, 0, .2, 0);

				if (ti % 20 == 0)
					MMOLib.plugin.getDamage().damage(stats.getPlayer(), target, new AttackResult(dps, DamageType.SKILL, DamageType.MAGIC), false);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 2);
		target.removePotionEffect(PotionEffectType.SLOW);
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) duration, (int) ability.getModifier("amplifier")));
	}
}
