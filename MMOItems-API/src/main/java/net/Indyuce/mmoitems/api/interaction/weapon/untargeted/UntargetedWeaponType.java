package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

/**
 * Used to determine if the item must be left or right clicked in order to
 * cast a basic attack. Whips, staffs are left click weapons whereas muskets
 * are right click weapons
 *
 * @author cympe
 */
public enum UntargetedWeaponType {
    RIGHT_CLICK,
    LEFT_CLICK;

    public boolean corresponds(@NotNull Action action) {
        return this == RIGHT_CLICK ?
                action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK :
                action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
    }
}
