package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Particle;

public class LightningSpirit implements StaffAttackHandler {

    @Override
    public void handle(AttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

        RayTrace trace = new RayTrace(attackMeta.getPlayer(), slot, range, entity -> UtilityMethods.canTarget(attackMeta.getPlayer(), entity, InteractionType.OFFENSE_ACTION));

        if (trace.hasHit())
            attackMeta.damage(trace.getHit());
        trace.draw(.5, loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
    }
}
