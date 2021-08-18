package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Slow extends TargetAbility {
    public Slow() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 5);
        addModifier("duration", 3);
        addModifier("amplifier", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        new BukkitRunnable() {
            final Location loc = target.getLocation();
            double ti = 0;

            public void run() {
                ti += Math.PI / 10;
                if (ti >= Math.PI * 2)
                    cancel();

                for (double j = 0; j < Math.PI * 2; j += Math.PI)
					for (double r = 0; r < .7; r += .1)
						loc.getWorld().spawnParticle(Particle.REDSTONE,
								loc.clone().add(Math.cos((ti / 2) + j + (Math.PI * r)) * r * 2, .1, Math.sin((ti / 2) + j + (Math.PI * r)) * r * 2),
								1, new Particle.DustOptions(Color.WHITE, 1));

			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LLAMA_ANGRY, 1, 2);
		target.addPotionEffect(
				new PotionEffect(PotionEffectType.SLOW, (int) (ability.getModifier("duration") * 20), (int) ability.getModifier("amplifier")));
	}
}
