package net.Indyuce.mmoitems.stat.data.type;

/**
 * <code>StatData</code> by itself is just a number, a boolean, an object...
 * <p>
 * These are paired with an <code>ItemStat</code> to mean something,
 * they then become the value of such <code>ItemStat</code>.
 * <p>
 * A stat data is enough to calculate the effects of a given item onto a
 * player but it not sufficient to take into accoun the full history of
 * an item.
 *
 * @author jules
 * @see {@link net.Indyuce.mmoitems.stat.type.StatHistory}
 */
public interface StatData {

    /**
     * @return <code>true</code> If this is the default state of the StatData, like an enchantment
     *         list data having 0 enchantments, or a percent bonus double stat having a value of 0.
     */
    public boolean isEmpty();
}
