package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.util.RayTrace;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;

public class LightningSpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

        RayTrace trace = new RayTrace(attackMeta.getPlayer(), range,
                entity -> MMOUtils.canTarget(attackMeta.getPlayer(), entity, InteractionType.OFFENSE_ACTION));

        if (trace.hasHit())
            attackMeta.applyEffectsAndDamage(nbt, trace.getHit());
        trace.draw(2, loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
    }
}
