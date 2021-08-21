package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Swiftness extends SimpleAbility {
    public Swiftness() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 15);
        addModifier("duration", 4);
        addModifier("amplifier", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration");
        int amplifier = (int) ability.getModifier("amplifier");

        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ZOMBIE_PIGMAN_ANGRY.toSound(), 1, .3f);
        for (double y = 0; y <= 2; y += .2)
            for (double j = 0; j < Math.PI * 2; j += Math.PI / 16)
                if (random.nextDouble() <= .7)
                    attack.getDamager().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getDamager().getLocation().add(Math.cos(j), y, Math.sin(j)), 0);
        attack.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        attack.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20), amplifier));
    }
}
