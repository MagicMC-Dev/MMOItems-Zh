package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Particle;

public class LightningSpirit implements StaffAttackHandler {

    @Override
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, EquipmentSlot slot, double range) {
        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

        RayTrace trace = new RayTrace(caster.getPlayer(), slot, range, entity -> UtilityMethods.canTarget(caster.getPlayer(), entity, InteractionType.OFFENSE_ACTION));

        if (trace.hasHit())
            caster.attack(trace.getHit(), damage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE);
        trace.draw(.5, loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
    }
}
