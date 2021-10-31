package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.FriendlyTargetAbility;
import net.Indyuce.mmoitems.ability.metadata.FriendlyTargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Regen_Ally extends FriendlyTargetAbility {
	public Regen_Ally() {
		super();

		addModifier("heal", 7);
		addModifier("duration", 3);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, FriendlyTargetAbilityMetadata ability) {
		LivingEntity target = ability.getTarget();

		new BukkitRunnable() {
			final double duration = Math.min(ability.getModifier("duration"), 60) * 20;
			final double hps = ability.getModifier("heal") / duration * 4;
			double ti = 0;
			double a = 0;

			public void run() {
				if (ti++ > duration || target.isDead()) {
					cancel();
					return;
				}

				a += Math.PI / 16;
				target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(1.3 * Math.cos(a), .3, 1.3 * Math.sin(a)), 0);

				if (ti % 4 == 0)
					MMOUtils.heal(target, hps);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
