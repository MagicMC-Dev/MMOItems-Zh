package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Blind extends TargetAbility {
    public Blind() {
        super();

        addModifier("duration", 5);
        addModifier("cooldown", 9);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 2);
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 24)
            for (double j = 0; j < 2; j++) {
                Location loc = target.getLocation();
                Vector vec = MMOUtils.rotateFunc(new Vector(Math.cos(i), 1 + Math.cos(i + (Math.PI * j)) * .5, Math.sin(i)),
                        attack.getPlayer().getLocation());
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(vec), 1, new Particle.DustOptions(Color.BLACK, 1));
            }
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (ability.getModifier("duration") * 20), 0));
	}
}
