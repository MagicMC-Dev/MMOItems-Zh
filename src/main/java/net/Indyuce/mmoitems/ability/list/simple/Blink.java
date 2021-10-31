package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Blink extends SimpleAbility {
    public Blink() {
        super();

        addModifier("range", 8);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attack.getPlayer().getLocation().add(0, 1, 0), 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
        Location loc = attack.getPlayer().getTargetBlock(null, (int) ability.getModifier("range")).getLocation().add(0, 1, 0);
        loc.setYaw(attack.getPlayer().getLocation().getYaw());
        loc.setPitch(attack.getPlayer().getLocation().getPitch());
        attack.getPlayer().teleport(loc);
        attack.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attack.getPlayer().getLocation().add(0, 1, 0), 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, attack.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
    }
}
