package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.NBTItem;
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
public interface SelfConsumable {

    /**
     * @return If the operation was successful, and thus the item
     *         must be consumed by one use.
     *         <br>
     *         Even a single <code>true</code> will make the item
     *         be consumed by one use, among all its Self-Consumable
     *         stats being triggered.
     */
    boolean onSelfConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player);
}
