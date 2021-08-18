package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Magma_Fissure extends TargetAbility {
    public Magma_Fissure() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
        addModifier("ignite", 4);
        addModifier("damage", 4);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        new BukkitRunnable() {
            final Location loc = attack.getDamager().getLocation().add(0, .2, 0);
            int j = 0;

            public void run() {
                j++;
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
                    cancel();
                    return;
                }

                Vector vec = target.getLocation().add(0, .2, 0).subtract(loc).toVector().normalize().multiply(.6);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 2, .2, 0, .2, 0);
                loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 1, 1);

                if (target.getLocation().distanceSquared(loc) < 1) {
                    loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1);
                    target.setFireTicks((int) (target.getFireTicks() + ability.getModifier("ignite") * 20));
                    new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage(target);
                    cancel();
                }
            }
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}