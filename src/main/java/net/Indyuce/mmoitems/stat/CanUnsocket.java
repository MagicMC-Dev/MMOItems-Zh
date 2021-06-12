package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;

/**
 * This item will be able to be used on other items, and
 * pop up an unsocketing GUI if it would make sense.
 *
 * @author Gunging
 */
public class CanUnsocket extends BooleanStat implements ConsumableItemInteraction {
    public CanUnsocket() {
        super("CAN_UNSOCKET", Material.PAPER, "Can Unsocket?",
                new String[] { "This item, when used on another item, if", "that other item has Gem Stones", "may be used to remove those Gems." },
                new String[] { "consumable" });
    }

    @Override
    public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {

        /*
         * Cancel if the target is just not an MMOItem
         */
        if (targetType == null) { return false; }

        /*
         * No Gemstones? No service
         */
        MMOItem mmo = new VolatileMMOItem(target);
        if (!mmo.hasData(ItemStats.GEM_SOCKETS)) { return false; }
        GemSocketsData mmoGems = (GemSocketsData) mmo.getData(ItemStats.GEM_SOCKETS);
        if (mmoGems == null || mmoGems.getGemstones().size() == 0) { return false; }
        Player player = playerData.getPlayer();

        /*
         * All right do it correctly I guess.
         *
         * Cancel if no gem could be extracted.
         */
        mmo = new LiveMMOItem(target);
        ArrayList<MMOItem> mmoGemStones = mmo.extractGemstones();
        if (mmoGemStones.size() == 0) {
            Message.RANDOM_UNSOCKET_GEM_TOO_OLD.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
            return false; }

        return true;
    }
}
