package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Prevent upgrade level from being lost when reforging.
 *
 * @author Gunging
 */
public class RFGKeepUpgrades implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        final UpgradeData upgrade = ((UpgradeData) event.getOldMMOItem().getData(ItemStats.UPGRADE));
        final UpgradeData newOne = ((UpgradeData) event.getNewMMOItem().getData(ItemStats.UPGRADE));

        if (!event.getOptions().shouldKeepUpgrades()
                || upgrade == null
                || newOne == null
                || newOne.getMaxUpgrades() <= 0)
            return;

        //UPGRD//MMOItems.log("  \u00a7e* \u00a77Existing Upgrade Detected");

        // Get current ig
        UpgradeData processed = new UpgradeData(newOne.getReference(), newOne.getTemplateName(), newOne.isWorkbench(), newOne.isDestroy(), newOne.getMax(), newOne.getMin(), newOne.getSuccess());

        // Edit level
        processed.setLevel(Math.min(upgrade.getLevel(), newOne.getMaxUpgrades()));
        //UPDT//MMOItems.log("  \u00a7e + \u00a77Set to level \u00a7f" + processed.getLevel() + " \u00a78Curr\u00a77 " + current.getLevel() + "\u00a78, Cache \u00a77" + cachedUpgradeLevel.getLevel());

        // Re-set cuz why not
        event.getNewMMOItem().setData(ItemStats.UPGRADE, processed);
    }
}
