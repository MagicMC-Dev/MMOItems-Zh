package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Frog_Mode extends SimpleAbility implements Listener {
    public Frog_Mode() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("duration", 20);
        addModifier("jump-force", 1);
        addModifier("speed", 1);
        addModifier("cooldown", 50);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 20;
        double y = ability.getModifier("jump-force");
        double xz = ability.getModifier("speed");

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                j++;
                if (j > duration)
                    cancel();

                if (attack.getDamager().getLocation().getBlock().getType() == Material.WATER) {
                    attack.getDamager().setVelocity(attack.getDamager().getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 / xz * y));
                    attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
                    for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                        attack.getDamager().getWorld().spawnParticle(Particle.CLOUD, attack.getDamager().getLocation(), 0, Math.cos(a), 0, Math.sin(a), .2);
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
