package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EditableEquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.InventoryUpdateHandler;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.type.NameData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeathDowngrading {

    /**
     * This will go through the following steps:
     *
     *  #1 Evaluate the list of equipped items {@link InventoryUpdateHandler#getEquipped()} to
     *     find those that can be death-downgraded.
     *
     *
     *  #2 Roll for death downgrade chances, downgrading the items
     *
     * @param player Player whose inventory is to be death-downgraded.
     */
    public static void playerDeathDowngrade(@NotNull Player player) {

        // Get Player
        PlayerData data = PlayerData.get(player);

        // Get total downgrade chance, anything less than zero is invalid
        double deathChance = data.getStats().getStat(ItemStats.DOWNGRADE_ON_DEATH_CHANCE);
        //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Current chance:\u00a7b " + deathChance);
        if (deathChance <= 0) { return; }

        // Make sure the equipped items list is up to date and retrieve it
        data.updateInventory();
        List<EquippedItem> items = data.getInventory().getEquipped();
        ArrayList<EditableEquippedItem> equipped = new ArrayList<>();

        // Equipped Player Items yeah...
        for (EquippedItem playerItem : items) {

            // Cannot downgrade? skip
            if (!canDeathDowngrade(playerItem)) { continue; }

            // Okay explore stat
            equipped.add((EditableEquippedItem) playerItem);
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Yes. \u00a7aAccepted");
        }

        // Nothing to perform operations? Snooze
        if (equipped.size() == 0) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 No items to downgrade. ");
            return; }

        // Create random
        Random random = new Random();

        // Degrade those items!
        while (deathChance >= 100 && equipped.size() > 0) {

            // Decrease
            deathChance -= 100;

            // Downgrade random item
            int deathChosen = random.nextInt(equipped.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            EditableEquippedItem equip = equipped.get(deathChosen);

            // Downgrade and remove from list
            equip.setItem(downgrade(new LiveMMOItem(equip.getNBT()), player));
            equipped.remove(deathChosen);

            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Autodegrading\u00a7a " + mmo.getData(ItemStats.NAME));
        }

        // If there is chance, and there is size, and there is chance success
        if (deathChance > 0 && equipped.size() > 0 && random.nextInt(100) < deathChance) {

            // Downgrade random item
            int d = random.nextInt(equipped.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            EditableEquippedItem equip = equipped.get(d);

            // Downgrade and remove from list
            equip.setItem(downgrade(new LiveMMOItem(equip.getNBT()), player));
            equipped.remove(d);

            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Chancedegrade\u00a7a " + mmo.getData(ItemStats.NAME));
        }
    }

    /**
     * @param allItems Absolutely all the items equipped by the player. This method will only affect
     *                 those that (A) can be downgraded, and (B) RNG roll to be downgraded.
     *
     * @param player Player whose items are being downgraded.
     *
     * @param deathChance Chance of downgrading one item of the list. Overrollable.
     *
     * @return The same list of items... but some of them possibly downgraded.
     */
    @NotNull public static ArrayList<ItemStack> downgradeItems(@NotNull List<ItemStack> allItems, @NotNull Player player, double deathChance) {

        // List of result and downgrade
        ArrayList<ItemStack> result = new ArrayList<>();
        ArrayList<ItemStack> downgrade = new ArrayList<>();

        // Choose
        for (ItemStack item : allItems) {

            // ?? Not that
            if (SilentNumbers.isAir(item)) { continue; }

            // Downgrade yay or nay
            if (canDeathDowngrade(item)) {

                // On to downgrading
                downgrade.add(item);

            } else {

                // On to result
                result.add(item);
            }
        }

        // Create random
        Random random = new Random();

        // Degrade those items!
        while (deathChance >= 100 && downgrade.size() > 0) {

            // Decrease
            deathChance -= 100;

            // Downgrade random item
            int deathChosen = random.nextInt(downgrade.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            ItemStack equip = downgrade.get(deathChosen);

            // Downgrade and remove from list
            result.add(downgrade(new LiveMMOItem(equip), player));

            // Remove this one item
            downgrade.remove(deathChosen);
        }

        // If there is chance, and there is size, and there is chance success
        if (deathChance > 0 && downgrade.size() > 0 && random.nextInt(100) < deathChance) {

            // Downgrade random item
            int deathChosen = random.nextInt(downgrade.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            ItemStack equip = downgrade.get(deathChosen);

            // Downgrade and remove from list
            result.add(downgrade(new LiveMMOItem(equip), player));

            // Remove this one item
            downgrade.remove(deathChosen);
        }

        // Those that survived are rejoined
        result.addAll(downgrade);

        // That's it
        return result;
    }

    /**
     * @param player For some reason I myself dont understand, {@link DurabilityItem} wants
     *               a non-null player to be able to perform durability operations.
     *
     * @param item Item to downgrade, make sure to have checked
     *             {@link #canDeathDowngrade(ItemStack)} before!
     *
     * @return This item but downgraded if it was possible.
     */
    @NotNull public static ItemStack downgrade(@NotNull ItemStack item, @NotNull Player player) {

        // No Item Meta I sleep
        if (SilentNumbers.isAir(item) || !item.getType().isItem()) { return item; }

        // Must be a MMOItem
        NBTItem asNBT = NBTItem.get(item);
        if (!asNBT.hasType()) { return item; }

        // Delegate to MMOItem Method
        return downgrade(new LiveMMOItem(asNBT), player);
    }

    /**
     * @param player For some reason I myself dont understand, {@link DurabilityItem} wants
     *               a non-null player to be able to perform durability operations.
     *
     * @param mmo Item to downgrade, make sure to have checked
     *             {@link #canDeathDowngrade(MMOItem)} before!
     *
     * @return This item but downgraded if it was possible.
     */
    @NotNull public static ItemStack downgrade(@NotNull LiveMMOItem mmo, @NotNull Player player) {

        mmo.getUpgradeTemplate().upgradeTo(mmo, mmo.getUpgradeLevel() - 1);

        // Build NBT
        ItemStack bakedItem = mmo.newBuilder().build();

        // Set durability to zero (full repair)
        DurabilityItem dur = new DurabilityItem(player, mmo.newBuilder().buildNBT());

        // Perform durability operations
        if (dur.getDurability() != dur.getMaxDurability()) {
            dur.addDurability(dur.getMaxDurability());
            bakedItem.setItemMeta(dur.toItem().getItemMeta());}

        // Send downgrading message
        Message.DEATH_DOWNGRADING.format(ChatColor.RED, "#item#", (mmo.getData(ItemStats.NAME) instanceof NameData) ? mmo.getData(ItemStats.NAME).toString() : SilentNumbers.getItemName(bakedItem, false))
                .send(player);

        // Uuuuh
        return bakedItem;
    }

    /**
     * @param player Player to check their death downgrade chance
     *
     * @return The death downgrade chance of the player
     */
    public static double getDeathDowngradeChance(@NotNull Player player) {

        // Get Player
        PlayerData data = PlayerData.get(player);

        // Get total downgrade chance, anything less than zero is invalid
        return data.getStats().getStat(ItemStats.DOWNGRADE_ON_DEATH_CHANCE);
    }

    /**
     * @param playerItem Equipped Item you want to know if it can be death downgraded
     *
     * @return If this is an instance of {@link EditableEquippedItem} and meets {@link #canDeathDowngrade(MMOItem)}
     */
    @Contract("null->false")
    public static boolean canDeathDowngrade(@Nullable EquippedItem playerItem) {

        // Null
        if (playerItem == null) { return false; }
        //DET//playerItem.getItem().hasData(ItemStats.NAME);
        //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Item:\u00a7b " + playerItem.getItem().getData(ItemStats.NAME));

        // Cannot perform operations of items that are uneditable
        if (!(playerItem instanceof EditableEquippedItem)) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not equippable. \u00a7cCancel");
            return false; }

        // Delegate to MMOItem Method
        return canDeathDowngrade(playerItem.getCached());
    }

    /**
     * @param playerItem Item you want to know if it can be death downgraded
     *
     * @return If this item is an MMOItem and meets {@link #canDeathDowngrade(MMOItem)}
     */
    @Contract("null->false")
    public static boolean canDeathDowngrade(@Nullable ItemStack playerItem) {

        // Null
        if (SilentNumbers.isAir(playerItem) || !playerItem.getType().isItem()) { return false; }
        //DET//playerItem.getItem().hasData(ItemStats.NAME);
        //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Item:\u00a7b " + playerItem.getItem().getData(ItemStats.NAME));

        // Get NBT
        NBTItem asNBT = NBTItem.get(playerItem);
        if (!asNBT.hasType()) { return false; }

        // Delegate to MMOItem Method
        return canDeathDowngrade(new VolatileMMOItem(asNBT));
    }

    /**
     * @param playerItem MMOItem you want to know if it can be death downgraded
     *
     * @return If this item has {@link ItemStats#DOWNGRADE_ON_DEATH} enabled, and
     *         has an upgrade template, and hasnt reached its minimum upgrades.
     */
    @Contract("null->false")
    public static boolean canDeathDowngrade(@Nullable MMOItem playerItem) {
        if (playerItem == null) { return false; }

        // Not downgradeable on death? Snooze
        if (!playerItem.hasData(ItemStats.DOWNGRADE_ON_DEATH)) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not Downgradeable. \u00a7cCancel");
            return false; }

        // No upgrade template no snooze
        if(!playerItem.hasData(ItemStats.UPGRADE)) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not Upgradeable. \u00a7cCancel");
            return false; }
        if (!playerItem.hasUpgradeTemplate()) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Null Template. \u00a7cCancel");
            return false; }

        // If it can be downgraded by one level...
        UpgradeData upgradeData = (UpgradeData) playerItem.getData(ItemStats.UPGRADE);
        //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Too downgraded? \u00a7c" + (upgradeData.getLevel() <= upgradeData.getMin()));

        return upgradeData.getLevel() > upgradeData.getMin();
    }
}
