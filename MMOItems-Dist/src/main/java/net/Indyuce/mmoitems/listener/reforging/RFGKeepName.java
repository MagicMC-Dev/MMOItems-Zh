package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.type.NameData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Transfers the name from the old MMOItem to the new one.
 *
 * This operation is intended to keep only the 'main name'
 * of the item (no modifier prefixes nor any of that)
 *
 * @author Gunging
 */
public class RFGKeepName implements Listener {

    @EventHandler
    @SuppressWarnings("OverlyStrongTypeCast")
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepName()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Name");

        // Yes
        NameData transfer;

        // Does it have name data?
        if (event.getOldMMOItem().hasData(ItemStats.NAME)) {

            // Transfer it to the new one
            NameData data = (NameData) event.getOldMMOItem().getData(ItemStats.NAME);

            // Make new one with main name
            transfer = new NameData(data.getMainName());

        // Well, got name?
        } else if (event.getReforger().getStack().getItemMeta().hasDisplayName()) {

            // That shall be the name of it
            transfer = new NameData(event.getReforger().getStack().getItemMeta().getDisplayName());

        // The item has no name
        } else {

            // No name no service
            return;
        }

        // All right set it as the original in the Stat History
        StatHistory hist = StatHistory.from(event.getNewMMOItem(), ItemStats.NAME);

        // Put it there
        ((NameData) hist.getOriginalData()).setString(transfer.getMainName());
    }
}
