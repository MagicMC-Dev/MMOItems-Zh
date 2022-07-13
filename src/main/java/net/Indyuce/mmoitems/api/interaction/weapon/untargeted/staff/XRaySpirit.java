package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import org.bukkit.Color;
import org.bukkit.Sound;

public class XRaySpirit implements StaffAttackHandler {

    @Override
    public void handle(AttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

        RayTrace trace = new RayTrace(attackMeta.getPlayer(), slot, range, entity -> UtilityMethods.canTarget(attackMeta.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            attackMeta.damage(trace.getHit());
        trace.draw(.5, Color.BLACK);
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
    }
}
