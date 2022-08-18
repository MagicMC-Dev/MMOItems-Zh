package net.Indyuce.mmoitems.listener.reforging;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

/**
 * Restores the old damage taken by the item, apparently
 * because RevID granted free repairs.
 *
 * @author Gunging
 */
public class RFGKeepDurability implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        final MMOItem newItem = event.getNewMMOItem(), oldItem = event.getOldMMOItem();

        // Unbreakable item
        if (event.getNewMMOItem().hasData(ItemStats.UNBREAKABLE) && ((BooleanData) event.getNewMMOItem().getData(ItemStats.UNBREAKABLE)).isEnabled())
            return;

        // Custom durability
        final @Nullable StatData customDurabilityData = oldItem.getData(ItemStats.CUSTOM_DURABILITY);
        if (customDurabilityData != null)
            newItem.setData(ItemStats.CUSTOM_DURABILITY, customDurabilityData);

        // Vanilla durability
        final @Nullable StatData vanillaDurabilityData = oldItem.getData(ItemStats.ITEM_DAMAGE);
        if (vanillaDurabilityData != null)
            newItem.setData(ItemStats.ITEM_DAMAGE, vanillaDurabilityData);
    }
}
