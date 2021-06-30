package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.random.UpdatableRandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Prevent previous RNG rolls of base stats
 * of items from being lost when reforging
 *
 * @author Gunging
 */
public class RFGKeepRNG implements Listener {

    public void onReforge(MMOItemReforgeEvent event) {

        // Rerolling stats? Nevermind
        if (event.getOptions().shouldReroll()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping RNG Rolls");

        /*
         * Proceed to go through all stats
         */
        for (ItemStat stat : event.getOldMMOItem().getStats()) {

            // Skip if it cant merge
            if (!(stat.getClearStatData() instanceof Mergeable)) { continue; }

            /*
             * These stats are exempt from this 'keeping' operation.
             * Probably because there is a ReforgeOption specifically
             * designed for them that keeps them separately
             */
            if (ItemStats.LORE.equals(stat) ||
                    ItemStats.NAME.equals(stat) ||
                    ItemStats.UPGRADE.equals(stat) ||
                    ItemStats.ENCHANTS.equals(stat) ||
                    ItemStats.SOULBOUND.equals(stat) ||
                    ItemStats.GEM_SOCKETS.equals(stat)) {

                continue; }

            // Stat history in the old item
            StatHistory hist = StatHistory.from(event.getOldMMOItem(), stat);

            // Alr what the template say, this roll too rare to be kept?
            RandomStatData source = event.getReforger().getTemplate().getBaseItemData().get(stat);
            StatData keptData = shouldRerollRegardless(stat, source, hist.getOriginalData(), event.getReforger().getGenerationItemLevel());

            // Old roll is ridiculously low probability under the new parameters. Forget.
            if (keptData == null) { return; }

            // Fetch History from the new item
            StatHistory clear = StatHistory.from(event.getNewMMOItem(), stat);

            // Replace original data of the new one with the roll from the old one
            clear.setOriginalData(keptData);
        }
    }

    /**
     * @return The item is supposedly being updated, but that doesnt mean all its stats must remain the same.
     *
     * 		   In contrast to reforging, in which it is expected its RNG to be rerolled, updating should not do it
     * 		   except in the most dire scenarios:
     * 		    + The mean/standard deviation changing significantly:
     * 		    	If the chance of getting the same roll is ridiculously low (3.5SD) under the new settings, reroll.
     *
     * 		    + The stat is no longer there, or a new stat was added
     * 		       The chance of getting a roll of 0 will be evaluated per the rule above.
     *
     *
     */
    @Nullable StatData shouldRerollRegardless(@NotNull ItemStat stat, @NotNull RandomStatData source, @NotNull StatData original, int determinedItemLevel) {

        // Not Mergeable, impossible to keep
        if (!(source instanceof UpdatableRandomStatData)) { return null; }

        // Just pass on
        return ((UpdatableRandomStatData) source).reroll(stat, original, determinedItemLevel);
    }

}
