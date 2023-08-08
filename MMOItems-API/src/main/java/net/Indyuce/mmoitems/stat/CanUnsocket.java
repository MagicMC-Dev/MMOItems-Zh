package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This item will be able to be used on other items, and
 * pop up an unsocketing GUI if it would make sense.
 *
 * @author Gunging
 */
public class CanUnsocket extends BooleanStat implements ConsumableItemInteraction {
    public CanUnsocket() {
        super("CAN_UNSOCKET", Material.PAPER, "能否提取宝石",
                new String[] { "此物品用于其他物品时", "如果其他物品上有宝石", "则可用于移除这些宝石" },
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
        final List<Pair<GemstoneData, MMOItem>> mmoGemStones = mmo.extractGemstones();
        if (mmoGemStones.isEmpty()) {
            Message.RANDOM_UNSOCKET_GEM_TOO_OLD.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
            return false; }

        return true;
    }
}
