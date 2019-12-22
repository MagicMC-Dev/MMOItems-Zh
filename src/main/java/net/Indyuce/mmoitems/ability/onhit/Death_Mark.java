package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.api.player.damage.AttackResult.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;

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
	public void whenCast(TemporaryStats stats, LivingEntity initialTarget, AbilityData data, AttackResult result) {
		LivingEntity target = initialTarget == null ? MMOItems.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		double duration = data.getModifier("duration") * 20;
		double dps = data.getModifier("damage") / duration * 20;

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
					MMOItems.plugin.getDamage().damage(stats.getPlayer(), target, new AttackResult(dps, DamageType.SKILL, DamageType.MAGICAL).applySkillEffects(stats, target), false);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 2);
		target.removePotionEffect(PotionEffectType.SLOW);
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) duration, (int) data.getModifier("amplifier")));
	}
}
