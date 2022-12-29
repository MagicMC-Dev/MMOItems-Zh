package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Very important makes the resulting ItemStack
 * have the same amount as the input one lmaoo
 *
 * @author Gunging
 */
public class RFFKeepAmount implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeFinishEvent event) {
        //RFG// MMOItems.log("§8Reforge §4RFF§7 Restoring Amount");

        // Set amount to original
        event.getFinishedItem().setAmount(event.getReforger().getNBTItem().getItem().getAmount());
    }
}
