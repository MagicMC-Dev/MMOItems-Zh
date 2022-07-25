package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.bukkit.Location;

import java.util.Random;

public interface StaffAttackHandler {
    static final Random RANDOM = new Random();

    void handle(PlayerMetadata caster, double damage, NBTItem nbt, EquipmentSlot slot, double range);

    default Location getGround(Location loc) {
        for (int j = 0; j < 20; j++) {
            if (loc.getBlock().getType().isSolid())
                return loc;
            loc.add(0, -1, 0);
        }
        return loc;
    }
}
