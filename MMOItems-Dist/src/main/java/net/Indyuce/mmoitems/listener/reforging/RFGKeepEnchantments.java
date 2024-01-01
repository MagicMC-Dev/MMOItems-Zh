package net.Indyuce.mmoitems.listener.reforging;

import io.lumine.mythic.lib.api.util.Ref;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Transfers enchantments from the old MMOItem to the new one.
 *
 * There
 *
 * @author Gunging
 */
public class RFGKeepEnchantments implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        if (!event.getOptions().shouldKeepEnchantments()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Enchants");

        // Enchant list data
        MMOItem operable = event.getOldMMOItem().clone();

        // Does it have MMOItems enchantment data?
        if (!operable.hasData(ItemStats.ENCHANTS)) {

            //RFG//MMOItems.log("  \u00a7b* \u00a77No Data, created blanc");
            operable.setData(ItemStats.ENCHANTS, new EnchantListData()); }

        //RFG//else { MMOItems.log("  \u00a7b* \u00a77Found Data"); }

        // Make sure they are consolidated
        Enchants.separateEnchantments(operable);

        // Gather
        StatHistory hist = StatHistory.from(operable, ItemStats.ENCHANTS);
        EnchantListData maybeOriginalEnchs = ((EnchantListData) hist.getOriginalData()).clone();

        //RFG//MMOItems.log("  \u00a7b*** \u00a77Enchantments in old item:");
        //RFG//hist.log();
        //RFG//log(maybeOriginalEnchs, "Ambiguous");

        // Whats in the new one
        //RFG//MMOItems.log("  \u00a7b*** \u00a77Enchantments in new item:");
        StatHistory future = StatHistory.from(event.getNewMMOItem(), ItemStats.ENCHANTS);
        //RFG//future.log();

        //RFG//MMOItems.log("  \u00a7b* \u00a77Transfer those already externals:"); int n = 0;
        // Just transfer those already registered as externals
        for (StatData pEnchants : hist.getExternalData()) {
            //RFG//log(maybeOriginalEnchs, "External #" + n++);

            // Put into the future one
            future.registerExternalData(pEnchants);
        }

        // Select from the supposed original, those that must have been put by players
        maybeOriginalEnchs.identifyTrueOriginalEnchantments(event.getNewMMOItem());
    }


    void log(@NotNull EnchantListData enchList, @NotNull String title) {
        MMOItems.print(null, "  \u00a73> \u00a77" + title +":", null);
        for (Enchantment e : enchList.getEnchants()) { MMOItems.print(null, "  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + enchList.getLevel(e), null); }
    }
}
