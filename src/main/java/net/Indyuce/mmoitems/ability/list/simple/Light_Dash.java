package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Light_Dash extends SimpleAbility {
    public Light_Dash() {
        super();

        addModifier("damage", 3);
        addModifier("cooldown", 10);
        addModifier("length", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double damage = ability.getModifier("damage");
        double length = ability.getModifier("length");

        new BukkitRunnable() {
            final Vector vec = attack.getPlayer().getEyeLocation().getDirection();
            final List<Integer> hit = new ArrayList<>();
            int j = 0;

            public void run() {
                if (j++ > 10 * Math.min(10, length))
                    cancel();

                attack.getPlayer().setVelocity(vec);
                attack.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, attack.getPlayer().getLocation().add(0, 1, 0), 0);
                attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 2);
                for (Entity entity : attack.getPlayer().getNearbyEntities(1, 1, 1))
                    if (!hit.contains(entity.getEntityId()) && MMOUtils.canTarget(attack.getPlayer(), entity)) {
                        hit.add(entity.getEntityId());
                        new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) entity);
                    }
            }
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
