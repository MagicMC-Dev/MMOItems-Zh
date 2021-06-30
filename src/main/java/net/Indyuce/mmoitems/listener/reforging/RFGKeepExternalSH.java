package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Keeps data registered onto the StatHistory by
 * 'external' sources.
 *
 * @author Gunging
 */
public class RFGKeepExternalSH implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepExternalSH()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping EXSH");

        // Through all the old histories
        for (StatHistory oldHist : event.getOldMMOItem().getStatHistories()) {

            // Skip enchantments tho
            if (oldHist.getItemStat() == ItemStats.ENCHANTS) { continue; }

            // Get newer
            StatHistory newHist = StatHistory.from(event.getNewMMOItem(), oldHist.getItemStat());

            // Through all EXSH
            for (StatData exSH : oldHist.getExternalData()) {

                // Add to the new one
                newHist.registerExternalData(exSH);
            }
        }
    }
}
