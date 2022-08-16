package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfers the lore from the old MMOItem to the new one.
 *
 * This operation is intended to allow refreshing the lore,
 * but keeping external things too.
 *
 * @author Gunging
 */
public class RFGKeepLore  implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepLore()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Lore");

        // No lore I sleep
        StringListData loreData = (StringListData) event.getOldMMOItem().getData(ItemStats.LORE);
        if (loreData == null) { return; }

        // Get lore of the item wth
        ArrayList<String> extraLore = extractLore(loreData.getList(), event.getOptions().getKeepCase());

        // No entries? snooze
        if (extraLore.size() == 0) { return; }

        // All right set it as the original in the Stat History
        StatHistory hist = StatHistory.from(event.getNewMMOItem(), ItemStats.LORE);

        // UUH just add it to the original I guess bruh
        StringListData original = (StringListData) hist.getOriginalData();
        for (String str : extraLore) { original.getList().add(str); }

        // Put it there
        hist.setOriginalData(original);
    }

    /**
     * From all this list, picks out only the elements that start with this case
     *
     * @param lore List of lore lines
     *
     * @param keepCase Case that must be matched to keep
     *
     * @return The lines which were successfully kept
     */
    @NotNull ArrayList<String> extractLore(@NotNull List<String> lore, @NotNull String keepCase) {

        //UPGRD//MMOItems.log(" \u00a7d> \u00a77Keeping Lore");
        ArrayList<String> ret = new ArrayList<>();

        // Examine every element
        for (String str : lore) {
            //UPGRD//MMOItems.log(" \u00a7d>\u00a7c-\u00a7e- \u00a77Line:\u00a7f " + str);

            // Does it start with the promised...?
            if (str.startsWith(keepCase)) {
                //UPGRD//MMOItems.log(" \u00a72>\u00a7a-\u00a7e- \u00a77Kept");
                ret.add(str); }
        }

        //UPGRD//MMOItems.log(" \u00a7d> \u00a77Result");
        //UPGRD//for (String lr : cachedLore) { MMOItems.log(" \u00a7d  + \u00a77" + lr); }
        return ret;
    }
}
