package net.Indyuce.mmoitems.listener.reforging;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import net.Indyuce.mmoitems.api.interaction.ItemSkin;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Transfers the lore from the old MMOItem to the new one.
 * <p>
 * This operation is intended to allow refreshing the lore,
 * but keeping external things too.
 *
 * @author Gunging
 */
public class RFFKeepSkins implements Listener {

    @EventHandler
    public void onReforge(MMOItemReforgeFinishEvent event) {
        if (!event.getOptions().shouldKeepSkins()) return;
        //RFG// MMOItems.log("§8Reforge §4EFG§7 Keeping Skins");

        // Apply skin to result
        final @Nullable String tagValue = event.getReforger().getNBTItem().getString(ItemSkin.SKIN_ID_TAG);
        if (tagValue != null && !tagValue.isEmpty()) {
            NBTItem resultAsNBT = NBTItem.get(event.getFinishedItem());
            ItemStack ret = ItemSkin.applySkin(resultAsNBT, new VolatileMMOItem(event.getReforger().getNBTItem()));
            event.setFinishedItem(ret);
        }
    }
}
