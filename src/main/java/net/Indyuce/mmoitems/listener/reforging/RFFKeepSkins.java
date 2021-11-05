package net.Indyuce.mmoitems.listener.reforging;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import net.Indyuce.mmoitems.api.interaction.ItemSkin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Transfers the lore from the old MMOItem to the new one.
 *
 * This operation is intended to allow refreshing the lore,
 * but keeping external things too.
 *
 * @author Gunging
 */
public class RFFKeepSkins implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeFinishEvent event) {
        if (!event.getOptions().shouldKeepSkins()) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Skins");

        // Got skin?
        if (!event.getReforger().getNBTItem().getBoolean(ItemSkin.tagHasSkin)) { return; }
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Item has skin");

        // Apply skin to result
        NBTItem resultAsNBT = NBTItem.get(event.getFinishedItem());

        // Apply skin
        ItemStack ret = ItemSkin.applySkin(resultAsNBT, event.getReforger().getNBTItem());

        // Success?
        if (ret != null) {
            //RFG// MMOItems.log("§8Reforge §4EFG§7 Success");
            event.setFinishedItem(ret); }
    }
}
