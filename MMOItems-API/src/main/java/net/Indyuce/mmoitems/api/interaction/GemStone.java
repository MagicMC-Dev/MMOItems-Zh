package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ApplyGemStoneEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.GemUpgradeScaling;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GemStone extends UseItem {
    @Deprecated
    public GemStone(Player player, NBTItem item) {
        super(player, item);
    }

    public GemStone(PlayerData player, NBTItem item) {
        super(player, item);
    }

    @NotNull
    public ApplyResult applyOntoItem(@NotNull NBTItem target, @NotNull Type targetType) {

        /*
         * Entirely loads the MMOItem and checks if
         * it has the required empty socket for the gem
         */
        return applyOntoItem(new LiveMMOItem(target), targetType, MMOUtils.getDisplayName(target.getItem()), true, false);
    }

    @NotNull
    public ApplyResult applyOntoItem(@NotNull MMOItem targetMMO, @NotNull Type targetType, @NotNull String itemName, boolean buildStack, boolean silent) {

        if (!targetMMO.hasData(ItemStats.GEM_SOCKETS))
            return new ApplyResult(ResultType.NONE);

        String gemType = getNBTItem().getString(ItemStats.GEM_COLOR.getNBTPath());

        GemSocketsData sockets = (GemSocketsData) targetMMO.getData(ItemStats.GEM_SOCKETS);
        String foundSocketColor = sockets.getEmptySocket(gemType);
        if (foundSocketColor == null)
            return new ApplyResult(ResultType.NONE);

        // Checks if the gem supports the item type, or the item set, or a weapon
        String appliableTypes = getNBTItem().getString(ItemStats.ITEM_TYPE_RESTRICTION.getNBTPath());
        if (!appliableTypes.isEmpty() && (!targetType.isWeapon() || !appliableTypes.contains("WEAPON"))
                && !appliableTypes.contains(targetType.getId()))
            return new ApplyResult(ResultType.NONE);

        // Check for success rate
        double successRate = getNBTItem().getStat(ItemStats.SUCCESS_RATE.getId());
        if (successRate == 0.0) successRate = 100;

        // Call the Bukkit event
        ApplyGemStoneEvent called = new ApplyGemStoneEvent(playerData, mmoitem, targetMMO,
                RANDOM.nextDouble() > successRate / 100 ? ResultType.FAILURE : ResultType.SUCCESS);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled() || called.getResult() == ResultType.NONE)
            return new ApplyResult(ResultType.NONE);

        // Return if gem stone application failure
        if (called.getResult() == ResultType.FAILURE) {
            if (!silent) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                Message.GEM_STONE_BROKE.format(ChatColor.RED, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", itemName).send(player);
            }

            return new ApplyResult(ResultType.FAILURE);
        }

        // To not clear enchantments put by players
        Enchants.separateEnchantments(targetMMO);

        /*
         * Gemstone can be successfully applied. Apply stats then abilities and
         * permanent effects. also REGISTER gemstone in the item gemstone list.
         */
        LiveMMOItem gemMMOItem = new LiveMMOItem(getNBTItem());
        GemstoneData gemData = new GemstoneData(gemMMOItem, foundSocketColor);

        /*
         * Now must apply the gem sockets data to the Stat History and then recalculate.
         * Gotta, however, find the correct StatData to which apply it to.
         */
        StatHistory gemStory = targetMMO.computeStatHistory(ItemStats.GEM_SOCKETS);
        findEmptySocket(gemStory, gemType, gemData);
        targetMMO.setData(ItemStats.GEM_SOCKETS, gemStory.recalculate(targetMMO.getUpgradeLevel()));

        /*
         * Get the item's level, important for the GemScalingStat
         */
        Integer levelIdentified = null;
        final String scaling = gemMMOItem.hasData(ItemStats.GEM_UPGRADE_SCALING) ? gemMMOItem.getData(ItemStats.GEM_UPGRADE_SCALING).toString() : GemUpgradeScaling.defaultValue;
        switch (scaling) {
            case "HISTORIC":
                levelIdentified = 0;
                break;
            case "SUBSEQUENT":
                levelIdentified = targetMMO.getUpgradeLevel();
                break;
            default:
                break;
        }
        gemData.setLevel(levelIdentified);

        // Only applies NON PROPER and MERGEABLE item stats
        for (ItemStat stat : gemMMOItem.getStats()) {
            if (stat instanceof GemStoneStat) continue;

            final StatData data = gemMMOItem.getData(stat);
            if (data instanceof Mergeable)
                targetMMO.mergeData(stat, data, gemData.getHistoricUUID());
        }

        if (!silent) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
            Message.GEM_STONE_APPLIED.format(ChatColor.YELLOW, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", itemName).send(player);
        }

        if (buildStack)
            return new ApplyResult(targetMMO.newBuilder().build());
        else
            return new ApplyResult(targetMMO, ResultType.SUCCESS);
    }

    private void findEmptySocket(StatHistory socketHistory, String gemType, GemstoneData gemstone) {

        // Og data
        GemSocketsData data = ((GemSocketsData) socketHistory.getOriginalData());
        if (data.apply(gemType, gemstone)) return;

        // Modifiers
        for (UUID modifierId : socketHistory.getAllModifiers()) {
            data = (GemSocketsData) socketHistory.getModifiersBonus(modifierId);
            if (data.apply(gemType, gemstone)) return;
        }

        // External
        for (StatData untypedData : socketHistory.getExternalData()) {
            data = (GemSocketsData) untypedData;
            if (data.apply(gemType, gemstone)) return;
        }

        // Gems
        for (UUID gemId : socketHistory.getAllGemstones()) {
            data = (GemSocketsData) socketHistory.getGemstoneData(gemId);
            if (data.apply(gemType, gemstone)) return;
        }

        throw new RuntimeException("MMOItem contains available socket but not its socket stat history");
    }

    public static class ApplyResult {
        @NotNull
        private final ResultType type;
        @Nullable
        private final ItemStack result;
        @Nullable
        private final MMOItem resultAsMMOItem;

        public ApplyResult(@NotNull ResultType type) {
            this((ItemStack) null, type);
        }

        public ApplyResult(@Nullable ItemStack result) {
            this(result, ResultType.SUCCESS);
        }

        public ApplyResult(@Nullable ItemStack result, @NotNull ResultType type) {
            this.type = type;
            this.result = result;
            this.resultAsMMOItem = null;
        }

        public ApplyResult(@Nullable MMOItem result, @NotNull ResultType type) {
            this.type = type;
            this.result = null;
            this.resultAsMMOItem = result;
        }

        @NotNull
        public ResultType getType() {
            return type;
        }

        @Nullable
        public ItemStack getResult() {
            return result;
        }

        @Nullable
        public MMOItem getResultAsMMOItem() {
            return resultAsMMOItem;
        }
    }

    public enum ResultType {

        /**
         * The gem stone is not successfully applied
         * onto the item and NEEDS to be destroyed
         */
        FAILURE,

        /**
         * The gem stone cannot be applied onto an item but the gem
         * MUST NOT be destroyed. Used when there are no available
         * gem sockets left or when the apply event is canceled.
         */
        NONE,

        /**
         * Gem stone is successfully applied and can be consumed
         */
        SUCCESS
    }
}
