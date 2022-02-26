package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
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
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

        RayTrace trace = new RayTrace(attackMeta.getPlayer(), slot, range, entity -> MMOUtils.canTarget(attackMeta.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            attackMeta.applyEffectsAndDamage(nbt, trace.getHit());
        trace.draw(.5, Color.BLACK);
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
    }
}
