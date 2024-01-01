package net.Indyuce.mmoitems.stat.data.random;

import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.jetbrains.annotations.NotNull;

/**
 * When updating items with the RevID system, an interesting case is when
 * looking into Random Stat Data, because one wouldn't want to reroll a good
 * roll in a player's item... unless this roll was unobtainable now, perhaps
 * the reason the item is getting updated is to fix that roll being too good...
 * <p>
 * Example of unobtainable data: the current numeric stat value is now out
 * of the numeric formula bounds (defined by max-spread).
 * <p>
 * This interface will tell the {@link net.Indyuce.mmoitems.api.util.MMOItemReforger}
 * if the current roll may be kept, or it is too extreme (under the updated metrics)
 * to be considered 'obtainable' and thus must be removed.
 * <p>
 * If a RandomStatData does not implement this, it will never be kept when
 * updating items (always be rerolled with latest settings).
 *
 * @author Gunging
 */
@FunctionalInterface
public interface UpdatableRandomStatData<S extends StatData> {

    /**
     * @param stat                In case its relevant, the stat this Stat Data is linked to
     * @param original            The StatData currently in the item being reforged.
     * @param determinedItemLevel The level of the item
     * @return The rerolled StatData if the original is unreasonable.
     * <br><br>
     * If the original is reasonable, a clone of it, probably using {@link StatData#clone()}
     */
    @NotNull
    S reroll(@NotNull ItemStat stat, @NotNull S original, int determinedItemLevel);
}
