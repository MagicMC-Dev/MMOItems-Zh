package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Prevents modifiers from being lost when reforging.
 *
 * @author Gunging
 */
public class RFGKeepModifications implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepMods()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Modifications");

        // Get rid of all Modifiers in the new item
        for (StatHistory newHist : event.getNewMMOItem().getStatHistories()) {

            // Clear those
            newHist.clearModifiersBonus();
        }

        // Through all the old histories
        for (StatHistory oldHist : event.getOldMMOItem().getStatHistories()) {

            // Get newer
            StatHistory newHist = StatHistory.from(event.getNewMMOItem(), oldHist.getItemStat());

            // Through all EXSH
            for (UUID mod : oldHist.getAllModifiers()) {

                // Get that data
                StatData modData = oldHist.getModifiersBonus(mod);

                // Snooze
                if (modData == null) { continue; }

                // Apply it
                newHist.registerModifierBonus(mod, ((Mergeable) modData).clone());
            }
        }
    }
}
