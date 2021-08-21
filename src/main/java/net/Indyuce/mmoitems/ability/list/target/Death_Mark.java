package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Death_Mark extends TargetAbility {
    public Death_Mark() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 7);
        addModifier("damage", 5);
        addModifier("duration", 3);
        addModifier("amplifier", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        double duration = ability.getModifier("duration") * 20;
        double dps = ability.getModifier("damage") / duration * 20;

        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti++;
                if (ti > duration || target == null || target.isDead()) {
                    cancel();
                    return;
                }

                target.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation(), 4, .2, 0, .2, 0);

                if (ti % 20 == 0)
                    new AttackMetadata(new DamageMetadata(dps, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage(target);
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 2);
		target.removePotionEffect(PotionEffectType.SLOW);
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) duration, (int) ability.getModifier("amplifier")));
	}
}
