package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.util.RayTrace;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class XRaySpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

        double a = Math.toRadians(attackMeta.getPlayer().getEyeLocation().getYaw() + 160);
        Location loc = attackMeta.getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

        RayTrace trace = new RayTrace(loc, attackMeta.getPlayer().getEyeLocation().getDirection(), range, entity -> MMOUtils.canTarget(attackMeta.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            attackMeta.applyEffectsAndDamage(nbt, trace.getHit());
        trace.draw(2, Color.BLACK);
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
    }
}
