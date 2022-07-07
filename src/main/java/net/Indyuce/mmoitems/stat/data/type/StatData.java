package net.Indyuce.mmoitems.stat.data.type;

/**
 * <code>StatData</code> by itself is just a number, a boolean, an object...
 * <p>
 * These are paired with an <code>ItemStat</code> to mean something,
 * they then become the value of such <code>ItemStat</code>.
 */
public interface StatData {

    /**
     * @return <code>true</code> If this is the default state of the StatData, like an enchantment
     *         list data having 0 enchantments, or a percent bonus double stat having a value of 0.
     */
    default boolean isEmpty() {
        // Backwards compatibility
        return this instanceof Mergeable && ((Mergeable) this).isClear();
    }
}
