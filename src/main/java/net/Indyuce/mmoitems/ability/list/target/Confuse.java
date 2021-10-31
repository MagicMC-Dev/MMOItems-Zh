package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Confuse extends TargetAbility {
    public Confuse() {
        super();

        addModifier("cooldown", 7);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHEEP_DEATH, 1, 2);
        new BukkitRunnable() {
            final Location loc = target.getLocation();
            final double rads = Math.toRadians(attack.getPlayer().getEyeLocation().getYaw() - 90);
            double ti = rads;

            public void run() {
                for (int j1 = 0; j1 < 3; j1++) {
                    ti += Math.PI / 15;
                    Location loc1 = loc.clone().add(Math.cos(ti), 1, Math.sin(ti));
                    loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1, 0);
                }
                if (ti >= Math.PI * 2 + rads)
                    cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		Location loc = target.getLocation().clone();
		loc.setYaw(target.getLocation().getYaw() - 180);
		target.teleport(loc);
	}
}
