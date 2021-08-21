package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Vampirism extends TargetAbility {
    public Vampirism() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 8);
        addModifier("drain", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        new BukkitRunnable() {
            final Location loc = target.getLocation();
            double ti = 0;
            double dis = 0;

            public void run() {
                for (int j1 = 0; j1 < 4; j1++) {
                    ti += .75;
                    dis += ti <= 10 ? .15 : -.15;

                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                        loc.getWorld().spawnParticle(Particle.REDSTONE,
                                loc.clone().add(Math.cos(j + (ti / 20)) * dis, 0, Math.sin(j + (ti / 20)) * dis), 1,
                                new Particle.DustOptions(Color.RED, 1));
                }
                if (ti >= 17)
                    cancel();
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_DRINK, 1, 2);
        MMOUtils.heal(attack.getDamager(), attack.getDamage().getDamage() * ability.getModifier("drain") / 100);
    }
}
