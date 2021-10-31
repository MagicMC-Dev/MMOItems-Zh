package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Burn extends TargetAbility {
    public Burn() {
        super();

        addModifier("duration", 3);
        addModifier("cooldown", 8);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

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
		target.setFireTicks((int) (target.getFireTicks() + ability.getModifier("duration") * 20));
	}
}
