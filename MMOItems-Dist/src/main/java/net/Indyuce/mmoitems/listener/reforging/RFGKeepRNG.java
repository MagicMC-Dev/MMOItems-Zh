package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.random.UpdatableRandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Prevent previous RNG rolls of base stats
 * of items from being lost when reforging
 *
 * @author Gunging
 */
public class RFGKeepRNG implements Listener {


    /**
     * The item is supposedly being updated, but that doesnt mean all its stats must remain the same.
     * <p>
     * In contrast to reforging, in which it is expected its RNG to be rerolled, updating should not do it
     * except in the most dire scenarios:
     * <br><br>
     * + The mean/standard deviation changing significantly:
     * If the chance of getting the same roll is ridiculously low (3.5SD) under the new settings, reroll.
     * <br><br>
     * + The stat is no longer there: Mean and SD become zero, so the rule above always removes the old roll.
     * <br><br>
     * + There is a new stat: The original data is null so this method cannot be called, will roll the
     * new stat to actually add it for the first time.
     */
    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        // Rerolling stats? Nevermind
        if (event.getOptions().shouldReRoll())
//            event.setCancelled(true);
            //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping new item (Complete RNG Reroll)");
            return;

        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping old RNG Rolls");

        event.getOldMMOItem().getStats()
                .forEach(stat -> {

                    // Check if stat can be transferred over new item
                    final RandomStatData source = event.getReforger().getTemplate().getBaseItemData().get(stat);
                    if (source == null || !(source instanceof UpdatableRandomStatData))
                        return;

                    /*
                     * Decide if this data is too far from RNG to
                     * preserve its rolls, even if it should be
                     * preserving the rolls.
                     */
                    final StatHistory hist = StatHistory.from(event.getOldMMOItem(), stat);
                    final StatData keptData = ((UpdatableRandomStatData) source).reroll(stat, hist.getOriginalData(), event.getReforger().getGenerationItemLevel());

                    // Old roll is ridiculously low probability under the new parameters. Forget.
                    if (keptData == null)
                        return;

                    // Fetch History from the new item
                    final StatHistory clear = StatHistory.from(event.getNewMMOItem(), stat);

                    // Replace original data of the new one with the roll from the old one
                    clear.setOriginalData(keptData);
                });
    }
}
