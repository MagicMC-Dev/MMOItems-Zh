package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Self consumables just means that the player eats this
 * item itself, rather than using it on another item.
 * <br>
 * Any food is a self consumable, since you eat them to
 * restore hunger or health or whatever, while upgrading
 * consumables are not self consumables, as the are used
 * on other items and cannot be consumed by themselves.
 *
 * @author Gunging
 */
@FunctionalInterface
public interface PlayerConsumable {

    /**
     * Called when the item is being consumed directly by a player.
     *
     * @return The return value is not being used anymore.
     */
    void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player);
}
