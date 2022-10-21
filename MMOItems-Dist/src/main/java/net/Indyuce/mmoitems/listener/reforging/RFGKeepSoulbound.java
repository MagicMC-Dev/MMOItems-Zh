package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Prevent soulbound from being lost when reforging.
 * Applies automatic soulbound config option.
 *
 * @author Gunging
 */
public class RFGKeepSoulbound implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        SoulboundData soul = (SoulboundData) event.getOldMMOItem().getData(ItemStats.SOULBOUND);

        // No data?
        if (soul == null) {

            // Auto soulbind active?
          //  if (MMOItems.plugin.getConfig().getBoolean("soulbound.auto-bind.disable-on." + event.getTypeName())) { return; }

            // Need a player to exist for auto soulbind
          //  if (event.getPlayer() == null) { return; }

            // Auto?
          //  if (event.getNewMMOItem().hasData(ItemStats.AUTO_SOULBIND) && !event.getNewMMOItem().hasData(ItemStats.SOULBOUND)) {

                // Auto soulbound config brr
          //      event.getNewMMOItem().setData(ItemStats.SOULBOUND, new SoulboundData(event.getPlayer().getUniqueId(), event.getPlayer().getName(), MMOItemReforger.autoSoulbindLevel));
          //  }

        } else if (event.getOptions().shouldKeepSoulBind()) {
            //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Soulbound");

            // Keep it
            event.getNewMMOItem().setData(ItemStats.SOULBOUND, soul);
        }
    }
}
