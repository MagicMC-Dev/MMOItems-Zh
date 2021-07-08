package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.NBTItem;

/**
 * The primordial problem is recalculating the item lore without having
 * to rebuild the entire item which would be too performance heavy.
 * <p>
 * Dynamic lore makes it so that you don't have to build it to update the lore.
 * <p>
 * See {@link net.Indyuce.mmoitems.api.item.util.DynamicLore}
 *
 * @author indyuce
 * @deprecated Now using {@link net.Indyuce.mmoitems.api.item.util.LoreUpdate}
 */
@Deprecated
public interface DynamicLoreStat {

    /**
     * The piece of text you would use in the lore format to reference this item stat.
     * <p>
     * Has to be lower case.
     *
     * @deprecated Not used anymore internally since dynamic placeholders are now
     * stored as every other placeholders in stats.yml
     */
    @Deprecated
    public String getDynamicLoreId();

    /**
     * Placeholder
     *
     * @param item
     * @return Null if the item does not have this stat,
     */
    public String calculatePlaceholder(NBTItem item);
}
