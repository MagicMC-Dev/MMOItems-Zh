package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Leap extends SimpleAbility {
    public Leap() {
        super();

        addModifier("force", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public SimpleAbilityMetadata canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability) {
        return attack.getPlayer().isOnGround() ? new SimpleAbilityMetadata(ability) : null;
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getPlayer().getLocation(), 16, 0, 0, 0.1);
        Vector vec = attack.getPlayer().getEyeLocation().getDirection().multiply(2 * ability.getModifier("force"));
        vec.setY(vec.getY() / 2);
        attack.getPlayer().setVelocity(vec);
        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti++;
                if (ti > 20)
                    cancel();

                attack.getPlayer().getWorld().spawnParticle(Particle.CLOUD, attack.getPlayer().getLocation().add(0, 1, 0), 0);
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
