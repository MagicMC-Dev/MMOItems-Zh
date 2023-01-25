package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import org.bukkit.Color;
import org.bukkit.Sound;

public class XRaySpirit implements StaffAttackHandler {

    @Override
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, EquipmentSlot slot, double range) {
        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

        RayTrace trace = new RayTrace(caster.getPlayer(), slot, range, entity -> UtilityMethods.canTarget(caster.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            caster.attack(trace.getHit(), damage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE);
        trace.draw(.5, Color.BLACK);
        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
    }
}
