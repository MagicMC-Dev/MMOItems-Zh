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

public class Wither extends TargetAbility {
    public Wither() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 8);
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
            double y = 0;

            public void run() {
                if (y > 3)
                    cancel();

                for (int j1 = 0; j1 < 3; j1++) {
					y += .07;
					for (int j = 0; j < 3; j++) {
						double a = y * Math.PI + (j * Math.PI * 2 / 3);
						double x = Math.cos(a) * (3 - y) / 2.5;
						double z = Math.sin(a) * (3 - y) / 2.5;
						loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(x, y, z), 1, new Particle.DustOptions(Color.BLACK, 1));
					}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		target.addPotionEffect(
				new PotionEffect(PotionEffectType.WITHER, (int) (ability.getModifier("duration") * 20), (int) ability.getModifier("amplifier")));
	}
}
