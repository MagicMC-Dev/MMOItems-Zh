package net.Indyuce.mmoitems.stat.data.type;

import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.jetbrains.annotations.NotNull;

/**
 * Most intuitive use is for ItemStats to not completely replace each other
 * when used through Gem Stones. This could happen for very complex options
 * like arrow particles, where only the while stat component makes sense (and
 * those behaviours are hardcoded for simplicity), but most of the time they
 * add up and get merged.
 * <p>
 * This serves a crucial internal role in determining which stats generate
 * {@link StatHistory}'s, which in turn allows them to be {@link Upgradable}.
 * <p></p>
 * <b>Strongly encouraged to override the <code>equals</code> method
 * to something fitting here as Mergeable stats should support comparisons.</b>
 */
public interface Mergeable<S extends StatData> extends StatData {

    /**
     * Merging two stat datas is required when an item benefits from
     * a buff in a stat, given by
     * - the base item data + 1 modifier
     * - at least 2 modifiers
     */
    void mergeWith(S data);

    /**
     * Returns a Data with the same values as this, but that is not this.
     */
    @NotNull
    S clone();
}
