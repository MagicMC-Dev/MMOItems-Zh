package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class XRaySpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, double range) {
        attackMeta.getDamager().getWorld().playSound(attackMeta.getDamager().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

        double a = Math.toRadians(attackMeta.getDamager().getEyeLocation().getYaw() + 160);
        Location loc = attackMeta.getDamager().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

        MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(attackMeta.getDamager(), range, entity -> MMOUtils.canTarget(attackMeta.getDamager(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            attackMeta.applyEffectsAndDamage(nbt, trace.getHit());
        trace.draw(loc, attackMeta.getDamager().getEyeLocation().getDirection(), 2, Color.BLACK);
        attackMeta.getDamager().getWorld().playSound(attackMeta.getDamager().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
    }
}
