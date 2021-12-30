package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Frozen_Aura extends SimpleAbility {
    public Frozen_Aura() {
        super();

        addModifier("duration", 6);
        addModifier("amplifier", 1);
        addModifier("radius", 10);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 20;
        double radiusSquared = Math.pow(ability.getModifier("radius"), 2);
        double amplifier = ability.getModifier("amplifier") - 1;

        new BukkitRunnable() {
            double j = 0;
            int ti = 0;

            public void run() {
                if (ti++ > duration)
					cancel();

				j += Math.PI / 60;
                for (double k = 0; k < Math.PI * 2; k += Math.PI / 2)
                    attack.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getPlayer().getLocation().add(Math.cos(k + j) * 2, 1 + Math.sin(k + j * 7) / 3, Math.sin(k + j) * 2), 0);

                if (ti % 2 == 0)
                    attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);

                if (ti % 7 == 0)
                    for (Entity entity : MMOUtils.getNearbyChunkEntities(attack.getPlayer().getLocation()))
                        if (entity.getLocation().distanceSquared(attack.getPlayer().getLocation()) < radiusSquared && MMOUtils.canTarget(attack.getPlayer(), entity)) {
                            ((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, (int) amplifier));
                        }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
