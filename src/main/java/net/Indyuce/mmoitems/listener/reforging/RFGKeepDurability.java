package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Restores the old damage taken by the item, apparently
 * because RevID granted free repairs.
 *
 * @author Gunging
 */
public class RFGKeepDurability implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {

        // What was its durability? Transfer it
        event.getNewMMOItem().setDamage(event.getOldMMOItem().getDamage());
    }
}
