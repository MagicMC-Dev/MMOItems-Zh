package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Leap extends SimpleAbility {
    public Leap() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("force", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public SimpleAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return attack.getDamager().isOnGround() ? new SimpleAbilityMetadata(ability) : null;
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 0);
        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getDamager().getLocation(), 16, 0, 0, 0.1);
        Vector vec = attack.getDamager().getEyeLocation().getDirection().multiply(2 * ability.getModifier("force"));
        vec.setY(vec.getY() / 2);
        attack.getDamager().setVelocity(vec);
        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti++;
                if (ti > 20)
                    cancel();

                attack.getDamager().getWorld().spawnParticle(Particle.CLOUD, attack.getDamager().getLocation().add(0, 1, 0), 0);
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
