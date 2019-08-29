package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Heavy_Charge extends Ability {
	public Heavy_Charge() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double knockback = data.getModifier("knockback");

		new BukkitRunnable() {
			double ti = 0;
			Vector vec = getTargetDirection(stats.getPlayer(), target).setY(-1);

			public void run() {
				ti++;
				if (ti > 20)
					cancel();

				if (ti < 9) {
					stats.getPlayer().setVelocity(vec);
					stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, stats.getPlayer().getLocation().add(0, 1, 0), 3, .13, .13, .13, 0);
				}

				for (Entity target : stats.getPlayer().getNearbyEntities(1, 1, 1))
					if (MMOUtils.canDamage(stats.getPlayer(), target)) {
						stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
						stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
						target.setVelocity(stats.getPlayer().getVelocity().setY(0.3).multiply(1.7 * knockback));
						stats.getPlayer().setVelocity(stats.getPlayer().getVelocity().setX(0).setY(0).setZ(0));
						new AttackResult(data.getModifier("damage"), DamageType.SKILL, DamageType.PHYSICAL).applyEffectsAndDamage(stats, null, (LivingEntity) target);
						cancel();
						break;
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
