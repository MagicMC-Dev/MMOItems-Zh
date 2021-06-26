package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.NBTItem;

/**
 * The primordial problem is recalculating the item lore
 * without having to rebuild the entire item which would
 * be too performance heavy.
 * <p>
 * Dynamic lore makes it so that you don't have to build it to update the lore.
 */
public interface DynamicLoreStat {

    /**
     * The piece of text you would use in the lore format to reference this item stat.
     * <p>
     * Has to be lower case.
     *
     * @deprecated We should simply use the lower case stat ID.
     */
    @Deprecated
    public String getDynamicLoreId();

    public String calculatePlaceholder(NBTItem item);
}
