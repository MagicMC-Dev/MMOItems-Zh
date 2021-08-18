package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Arcane_Rift extends Ability<VectorAbilityMetadata> {
    public Arcane_Rift() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 5);
        addModifier("amplifier", 2);
        addModifier("cooldown", 10);
        addModifier("speed", 1);
        addModifier("duration", 1.5);
        addModifier("mana", 0);
        addModifier("stamina", 0);
	}

    @Override
    public VectorAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return attack.getDamager().isOnGround() ? new VectorAbilityMetadata(ability, attack.getDamager(), target) : null;
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        double damage = ability.getModifier("damage");
        double slowDuration = ability.getModifier("duration");
        double slowAmplifier = ability.getModifier("amplifier");

        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ENDERMAN_DEATH.toSound(), 2, .5f);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(0).normalize().multiply(.5 * ability.getModifier("speed"));
            final Location loc = attack.getDamager().getLocation();
            final int duration = (int) (20 * Math.min(ability.getModifier("duration"), 10.));
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                if (ti++ > duration)
                    cancel();

                loc.add(vec);
                loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 5, .5, 0, .5, 0);

                for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                    if (MMOUtils.canDamage(attack.getDamager(), entity) && loc.distanceSquared(entity.getLocation()) < 2 && !hit.contains(entity.getEntityId())) {
                        hit.add(entity.getEntityId());
                        new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
                    }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
