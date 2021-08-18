package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Blink extends SimpleAbility {
    public Blink() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("range", 8);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attack.getDamager().getLocation().add(0, 1, 0), 0);
        attack.getDamager().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getDamager().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
        Location loc = attack.getDamager().getTargetBlock(null, (int) ability.getModifier("range")).getLocation().add(0, 1, 0);
        loc.setYaw(attack.getDamager().getLocation().getYaw());
        loc.setPitch(attack.getDamager().getLocation().getPitch());
        attack.getDamager().teleport(loc);
        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attack.getDamager().getLocation().add(0, 1, 0), 0);
        attack.getDamager().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getDamager().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
    }
}
