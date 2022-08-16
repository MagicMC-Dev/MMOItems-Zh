package net.Indyuce.mmoitems.stat.data.type;

import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.jetbrains.annotations.NotNull;

/**
 * Most intuitive use is for ItemStats to not completely replace each other
 * when used through Gem Stones. However, this serves a crucial internal
 * role in determining which stats generate {@link StatHistory}'s, which in
 * turn allows them to be {@link Upgradable}.
 * <p></p>
 * <b>Strongly encouraged to override the <code>equals</code> method
 * to something fitting here as Mergeable stats should support comparisons.</b>
 */
public interface Mergeable<S extends StatData> extends StatData {

    /**
     * Merging two stat data is used when either applying a gem stone to an item
     * which already has this type of item data, or when generating an item
     * randomly so that the item benefits from all modifiers
     */
    void merge(S data);

    /**
     * Returns a Data with the same values as this, but that is not this.
     */
    @NotNull
    S cloneData();

    /**
     * @return <code>true</code> If this is the default state of the StatData, like an enchantment
     *         list data having 0 enchantments, or a percent bonus double stat having a value of 0.
     */
    @Deprecated
    default boolean isClear() {
        // Backwards compatibility
        return isEmpty();
    }
}
