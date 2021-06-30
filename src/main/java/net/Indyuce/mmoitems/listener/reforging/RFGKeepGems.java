package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Prevents gems from being lost when reforging.
 * Will also drop them to the ground as items if
 * they don't fit the new item.
 *
 * @author Gunging
 */
public class RFGKeepGems implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepGemStones()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Gems");

        // Get those gems
        GemSocketsData gems = (GemSocketsData) event.getOldMMOItem().getData(ItemStats.GEM_SOCKETS);

        // No gems? why are we here
        if (gems == null) { return; }

        // Get those gems
        GemSocketsData current = (GemSocketsData) event.getNewMMOItem().getData(ItemStats.GEM_SOCKETS);

        //RFG//MMOItems.log(" \u00a7a@ \u00a77Applying Gemstones");
        ArrayList<GemstoneData> lostGems = new ArrayList<>();

        // If has a upgrade template defined, just remember the level
        if (current != null) {

            // Get current ig
            //RFG//MMOItems.log("  \u00a7a* \u00a77Existing Data Detected\u00a7a " + current.toString());

            // Get those damn empty sockets
            ArrayList<GemstoneData> putGems = new ArrayList<>();
            ArrayList<String> availableSockets = new ArrayList<>(current.getEmptySlots());
            ArrayList<GemstoneData> oldSockets = new ArrayList<>(gems.getGemstones());

            // Remaining
            for (GemstoneData data : oldSockets) {
                //RFG//MMOItems.log("  \u00a7a*\u00a7e* \u00a77Fitting \u00a7f" + data.getHistoricUUID().toString() + "\u00a77 '" + data.getName());

                // No more if no more sockets left
                if (availableSockets.size() <= 0) {
                    //RFG//MMOItems.log(" \u00a7a  +\u00a7c+ \u00a77No More Sockets");

                    // This gemstone could not be inserted, it is thus lost
                    lostGems.add(data);
                    //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno socket \u00a78" + data.getHistoricUUID());

                    // Still some sockets to fill hMMM
                } else {

                    // Get colour, uncolored if Unknown
                    String colour = data.getSocketColor();
                    if (colour == null) { colour = GemSocketsData.getUncoloredGemSlot(); }
                    String remembrance = null;

                    // Does the gem data have an available socket?
                    for (String slot : availableSockets) { if (slot.equals(GemSocketsData.getUncoloredGemSlot()) || colour.equals(slot)) { remembrance = slot; } }

                    // Existed?
                    if (remembrance != null) {
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone fit - \u00a7e " + remembrance + " \u00a78" + data.getHistoricUUID());

                        // Remove
                        availableSockets.remove(remembrance);

                        // And guess what... THAT is the colour of this gem! Fabulous huh?
                        data.setColour(remembrance);

                        // Remember as a put gem
                        putGems.add(data);

                        // Scourge the old item's stat histories and transfer all stats related to this gem
                        for (StatHistory oldHist : event.getOldMMOItem().getStatHistories()) {

                            // For every gem
                            for (UUID oldHistGem : oldHist.getAllGemstones()) {

                                //RFG// MMOItems.log("§8Reforge §4GEM§7 Gemstone of\u00a73 " + oldHist.getItemStat().getId() + "\u00a7e " + oldHistGem);

                                // Matches?
                                if (oldHistGem.equals(data.getHistoricUUID())) {
                                    //RFG// MMOItems.log("§8Reforge §4GEM§7 Match!");

                                    // Get the gem data
                                    StatData sData = oldHist.getGemstoneData(oldHistGem);

                                    if (!(sData instanceof Mergeable)) { continue; }

                                    // Put it there
                                    StatHistory newHist = StatHistory.from(event.getNewMMOItem(), oldHist.getItemStat());

                                    // Include yes
                                    newHist.registerGemstoneData(oldHistGem, ((Mergeable) sData).cloneData());
                                }
                            }
                        }

                    // No space/valid socket hmm
                    } else {
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno color \u00a78" + data.getHistoricUUID());

                        // Include as lost gem
                        lostGems.add(data); }
                }
            }

            // Create with select socket slots and gems
            GemSocketsData primeGems = new GemSocketsData(availableSockets);
            for (GemstoneData gem : putGems) { if (gem == null) { continue; } primeGems.add(gem); }
            //RFG//MMOItems.log("  \u00a7a* \u00a77Operation Result\u00a7a " + primeGems.toString());

            // That's the original data
            StatHistory gemStory = StatHistory.from(event.getNewMMOItem(), ItemStats.GEM_SOCKETS);
            gemStory.setOriginalData(primeGems);
            //RFG//MMOItems.log("  \u00a7a* \u00a77History Final\u00a7a --------");
            //RFG//gemStory.log();

        // Could not fit any gems: No gem sockets!
        } else {
            //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 All gemstones were lost -  \u00a7cno data");

            // ALl were lost
            lostGems.addAll(gems.getGemstones()); }

        // Config option enabled? Build the lost gem MMOItems!
        if (ReforgeOptions.dropRestoredGems) {
            for (GemstoneData lost : lostGems) {

                // Get MMOItem
                MMOItem restoredGem = event.getOldMMOItem().extractGemstone(lost);

                // Success? Add that gem there
                if (restoredGem != null) { event.getReforger().addReforgingOutput(restoredGem.newBuilder().build()); } } }
    }
}
