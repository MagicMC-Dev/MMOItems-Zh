package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Earthquake extends Ability<VectorAbilityMetadata> {
    public Earthquake() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 3);
        addModifier("duration", 2);
        addModifier("amplifier", 1);
        addModifier("cooldown", 10);
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

        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(0);
            final Location loc = attack.getDamager().getLocation();
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                ti++;
                if (ti > 20)
                    cancel();

                loc.add(vec);
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .5, 0, .5, 0);
                loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 2, 1);

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
