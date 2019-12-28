
package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;

public class Regen_Ally extends Ability {
	public Regen_Ally() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("heal", 7);
		addModifier("duration", 3);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new FriendlyTargetAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		LivingEntity target = ((FriendlyTargetAbilityResult) ability).getTarget();

		new BukkitRunnable() {
			double ti = 0;
			double a = 0;
			double duration = Math.min(ability.getModifier("duration"), 60) * 20;
			double hps = ability.getModifier("heal") / duration * 4;

			public void run() {
				ti++;
				if (ti > duration || target.isDead()) {
					cancel();
					return;
				}

				a += Math.PI / 16;
				target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(1.3 * Math.cos(a), .3, 1.3 * Math.sin(a)), 0);

				if (ti % 4 == 0)
					MMOUtils.heal((Player) target, hps);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	/*
	 * TargetAbilityResult but only targets players
	 */
	public class FriendlyTargetAbilityResult extends AbilityResult {
		private final LivingEntity target;

		public FriendlyTargetAbilityResult(AbilityData ability, Player caster, LivingEntity target) {
			super(ability);

			this.target = target != null ? target : MMOLib.plugin.getVersion().getWrapper().rayTrace(caster, 50, entity -> (entity instanceof Player && MMOUtils.canDamage(caster, entity))).getHit();
		}

		public LivingEntity getTarget() {
			return target;
		}

		@Override
		public boolean isSuccessful() {
			return target != null;
		}
	}
}
