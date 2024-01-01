package net.Indyuce.mmoitems.listener.reforging;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

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
        GemSocketsData oldGemstoneData = (GemSocketsData) event.getOldMMOItem().getData(ItemStats.GEM_SOCKETS);

        // No gems? why are we here
        if (oldGemstoneData == null) { return; }

        // Get those gems
        GemSocketsData newGemstoneData = (GemSocketsData) event.getNewMMOItem().getData(ItemStats.GEM_SOCKETS);

        //RFG//MMOItems.log(" \u00a7a@ \u00a77Applying Gemstones");
        ArrayList<GemstoneData> lostGems = new ArrayList<>();

        // If it has an upgrade template defined, just remember the level
        if (newGemstoneData != null) {

            // Get current ig
            //RFG//MMOItems.log("  \u00a7a* \u00a77Existing Data Detected\u00a7a " + oldGemstoneData.toString());

            // Get those damn empty sockets
            ArrayList<GemstoneData> transferredGems = new ArrayList<>();
            ArrayList<String> availableSockets = new ArrayList<>(newGemstoneData.getEmptySlots());
            ArrayList<GemstoneData> oldSockets = new ArrayList<>(oldGemstoneData.getGemstones());

            // Remaining
            for (GemstoneData oldSocketedGem : oldSockets) {
                //RFG//MMOItems.log("  \u00a7a*\u00a7e* \u00a77Fitting \u00a7f" + oldSocketedGem.getHistoricUUID().toString() + "\u00a77 '" + oldSocketedGem.getName() + "\u00a7'");

                // No more if no more sockets left
                if (availableSockets.size() == 0) {
                    //RFG//MMOItems.log(" \u00a7a  +\u00a7c+ \u00a77No More Sockets");

                    // This gemstone could not be inserted, it is thus lost
                    lostGems.add(oldSocketedGem);
                    //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno socket \u00a78" + oldSocketedGem.getHistoricUUID());

                    // Still some sockets to fill hMMM
                } else {

                    // Get colour, uncolored if Unknown
                    String colour = oldSocketedGem.getSocketColor();
                    if (colour == null) { colour = GemSocketsData.getUncoloredGemSlot(); }
                    String newColorToInsertInto = null;

                    // Does the gem data have an available socket?
                    for (String slot : availableSockets) { if (slot.equals(GemSocketsData.getUncoloredGemSlot()) || colour.equals(slot)) { newColorToInsertInto = slot; } }

                    // Existed?
                    if (newColorToInsertInto != null) {
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77\u00a7e ----------------------- ");
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone fit - \u00a7e " + newColorToInsertInto + " \u00a78" + oldSocketedGem.getHistoricUUID());

                        // Get MMOItem
                        MMOItem restoredGem = event.getOldMMOItem().extractGemstone(oldSocketedGem);
                        if (restoredGem == null) {
                            //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a7c Cannot rebuild gem Item Stack");

                            // Include as lost gem
                            lostGems.add(oldSocketedGem);
                            continue; }

                        // Reforge gemstone
                        MMOItemReforger gemReforge = new MMOItemReforger(restoredGem.newBuilder().build());
                        if (!gemReforge.canReforge()) {
                            //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a7c Cannot reforge gem MMOItem");

                            // Include as lost gem
                            lostGems.add(oldSocketedGem);
                            continue;
                        }
                        gemReforge.setPlayer(event.getPlayer());
                        if (!gemReforge.reforge(MMOItems.plugin.getLanguage().gemRevisionOptions)) {
                            //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a7c Refoge event cancelled");

                            // Include as lost gem
                            lostGems.add(oldSocketedGem);
                            continue;
                        }
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Successfully\u00a7a applying gem");

                        // Gems should not be dropping stuff but whatever
                        event.getReforger().getReforgingOutput().addAll(gemReforge.getReforgingOutput());

                        // Whatever
                        LiveMMOItem gemResult = new LiveMMOItem(gemReforge.getResult());
                        GemstoneData reforgedGemData = new GemstoneData(gemResult, newColorToInsertInto, oldSocketedGem.getHistoricUUID());
                        reforgedGemData.setLevel(oldSocketedGem.getLevel());

                        // Remove, we have succeeded
                        availableSockets.remove(newColorToInsertInto);

                        // Gem has been transferred (basically)
                        transferredGems.add(reforgedGemData);

                        // Apply gemstone stats into the item
                        for (ItemStat stat : gemResult.getStats()) {

                            // If it is not PROPER
                            if (!(stat instanceof GemStoneStat)) {

                                // Get the stat data
                                StatData data = gemResult.getData(stat);

                                // If the data is MERGEABLE
                                if (data instanceof Mergeable) {

                                    // Just ignore that lol
                                    if (data instanceof EnchantListData && data.isEmpty()) { continue; }

                                    //RFG//MMOItems.log("\u00a79>>> \u00a77Gem-Merging \u00a7c" + stat.getNBTPath() + "\u00a7e" + data.toString() + "\u00a78 " + reforgedGemData.getHistoricUUID().toString());

                                    /*
                                     * The gem data is registered directly into the history (emphasis on not recalculating with purge)
                                     */
                                    StatHistory hist = StatHistory.from(event.getNewMMOItem(), stat);
                                    hist.registerGemstoneData(reforgedGemData.getHistoricUUID(), data);

                                    //RFG//MMOItems.log("\u00a79>>> \u00a77 Stat history of this stat");
                                    //RFG//hist.log();
                                }
                            }
                        }

                    // No space/valid socket hmm
                    } else {
                        //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno color \u00a78" + oldSocketedGem.getHistoricUUID());

                        // Include as lost gem
                        lostGems.add(oldSocketedGem); }
                }
            }

            // Create with select socket slots and gems
            GemSocketsData reforgedGemstoneData = new GemSocketsData(availableSockets);
            for (GemstoneData gem : transferredGems) { if (gem == null) { continue; } reforgedGemstoneData.add(gem); }
            //RFG//MMOItems.log("  \u00a7a* \u00a77Operation Result\u00a7a " + reforgedGemstoneData.toString());
            //RFG//for (String s : reforgedGemstoneData.getEmptySlots()) { MMOItems.log("  \u00a7a* \u00a77Empty\u00a7f " + s); }
            //RFG//for (GemstoneData s : reforgedGemstoneData.getGemstones()) { MMOItems.log("  \u00a7a*\u00a7f " + s.getName() + "\u00a77 " + s.getHistoricUUID()); }

            // That's the original data
            StatHistory gemStory = StatHistory.from(event.getNewMMOItem(), ItemStats.GEM_SOCKETS);
            gemStory.setOriginalData(reforgedGemstoneData);
            event.getNewMMOItem().setData(ItemStats.GEM_SOCKETS, gemStory.recalculate(event.getNewMMOItem().getUpgradeLevel()));
            //RFG//MMOItems.log("  \u00a7a* \u00a77History Final\u00a7a --------");
            //RFG//gemStory.log();

        // Could not fit any gems: No gem sockets!
        } else {
            //RFG//MMOItems.log("\u00a7c *\u00a7e*\u00a77 All gemstones were lost -  \u00a7cno data");

            // ALl were lost
            lostGems.addAll(oldGemstoneData.getGemstones()); }

        // Config option enabled? Build the lost gem MMOItems!
        if (ReforgeOptions.dropRestoredGems) {
            for (GemstoneData lost : lostGems) {

                // Get MMOItem
                MMOItem restoredGem = event.getOldMMOItem().extractGemstone(lost);
                if (restoredGem == null) {

                    // Mention the loss
                    MMOItems.print(null, "$bGemstone $r{0} {1} $bno longer exists, it was$f deleted$b from $u{2}$b's {3}$b. ", "RevID", lost.getMMOItemType(), lost.getMMOItemID(), event.getPlayer() == null ? "null" : event.getPlayer().getName(), SilentNumbers.getItemName(event.getReforger().getStack(), false));
                    continue; }

                // Reforge gemstone if it can be reforged
                MMOItemReforger gemReforge = new MMOItemReforger(restoredGem.newBuilder().build());
                if (gemReforge.canReforge()) {

                    // Reforge
                    gemReforge.setPlayer(event.getPlayer());
                    gemReforge.reforge(MMOItems.plugin.getLanguage().gemRevisionOptions);

                    // Gems should not be dropping stuff but whatever
                    event.getReforger().getReforgingOutput().addAll(gemReforge.getReforgingOutput());

                    // Success, Add that reforged gem
                    event.getReforger().addReforgingOutput(gemReforge.getResult());

                    // Cant reforge (uuuuuh) just add I guess
                } else {

                    // Success? Add that gem (without reforging) there
                    event.getReforger().addReforgingOutput(restoredGem.newBuilder().build());
                }
            } }
    }
}
