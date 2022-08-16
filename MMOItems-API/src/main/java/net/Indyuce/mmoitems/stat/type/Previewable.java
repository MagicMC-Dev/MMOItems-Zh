package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

/**
 * Suppose this stat may display different in an item before
 * it is crafted (say, a double stat displays a range of the
 * values it can have) than when its crafted (where the double
 * stat displays the RNG result it got when crafted)
 *
 * @author Gunging
 */
public interface Previewable<R extends RandomStatData<S>, S extends StatData> {

    /**
     * Literally a copy of {@link ItemStat#whenApplied(ItemStackBuilder, StatData)}
     * but that puts the 'preview lore' in instead of the actual stat lore.
     *
     * @param item Item being built
     * @param currentData Current Data of the item
     * @param templateData Random Data of the item
     *
     * @throws IllegalArgumentException If something go wrong
     */
    void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull S currentData, @NotNull R templateData) throws IllegalArgumentException;
}
