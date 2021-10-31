package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Shock extends TargetAbility {
    public Shock() {
        super();

        addModifier("duration", 2);
        addModifier("cooldown", 8);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        double duration = ability.getModifier("duration");

        target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ZOMBIE_PIGMAN_ANGRY.toSound(), 1, 2);
        new BukkitRunnable() {
            final Location loc = target.getLocation();
            final double rads = Math.toRadians(attack.getPlayer().getEyeLocation().getYaw() - 90);
            double ti = rads;

            public void run() {
                for (int j = 0; j < 3; j++) {
                    ti += Math.PI / 15;
                    target.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(Math.cos(ti), 1, Math.sin(ti)), 0);
                }
                if (ti >= Math.PI * 2 + rads)
                    cancel();
            }
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		new BukkitRunnable() {
			int ti;

			public void run() {
				if (ti++ > (duration > 300 ? 300 : duration * 10) || target.isDead())
					cancel();
				else
					target.playEffect(EntityEffect.HURT);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
