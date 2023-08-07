package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A class to manage modification of items with reference to what
 * they used to be. Updating/reforging refers to changing the base
 * stats of a MMOItem instance to what the template currently has,
 * usually keeping gem stones and upgrade level. This won't reroll
 * RNG stats unless the specific option is toggled on.
 *
 * @author Gunging, Jules
 */
public class MMOItemReforger {

    /**
     * Create this reforger to handle all operations regarding RevID
     * increases on any ItemStack, including: 									<br>
     * * Make a fresh version					<br>
     * * Transfer stats from old to fresh 	<br>
     * * Build the fresh version	 			<br>
     *
     * @param stack The ItemStack you want to update, or at least
     *              know if it should update due to RevID increase.
     *              <br><br>
     *              Gets the NBTItem from {@link NBTItem#get(ItemStack)}
     */
    public MMOItemReforger(@NotNull ItemStack stack) {
        this(NBTItem.get(stack));
    }

    /**
     * @param nbtItem If for any reason you already generated an NBTItem,
     *                you may pass it here to ease the performance of
     *                generating it again from the ItemStack.
     * @param stack   Useless parameter.
     */
    @Deprecated
    public MMOItemReforger(@Nullable ItemStack stack, @NotNull NBTItem nbtItem) {
        this(nbtItem);
    }

    /**
     * Create this reforger to handle all operations regarding RevID
     * increases on any ItemStack, including: 									<br>
     * * Make a fresh version					<br>
     * * Transfer stats from old to fresh 	<br>
     * * Build the fresh version	 			<br>
     *
     * @param nbtItem If for any reason you already generated an NBTItem,
     *                you may pass it here I guess, the ItemStack will be
     *                regenerated through {@link NBTItem#getItem()}
     */
    public MMOItemReforger(@NotNull NBTItem nbtItem) {
        this.nbtItem = nbtItem;

        Validate.isTrue(nbtItem.getItem().getItemMeta() != null, "ItemStack has no ItemMeta, cannot be reforged.");

        // Try and find corresponding item template.
        template = nbtItem.hasType() ? MMOItems.plugin.getTemplates().getTemplate(nbtItem) : null;
    }

    /**
     * The original NBTItem information.
     */
    @NotNull
    private final NBTItem nbtItem;

    /**
     * @return The original NBTItem information.
     */
    @NotNull
    public NBTItem getNBTItem() {
        return nbtItem;
    }

    /**
     * @return The original ItemStack, not even a clone.
     */
    @NotNull
    public ItemStack getStack() {
        return nbtItem.getItem();
    }

    /**
     * The original ItemStack itself, not even a clone.
     */
    @Nullable
    private ItemStack result;

    /**
     * @return The original ItemStack, not even a clone.
     */
    @Nullable
    public ItemStack getResult() {
        return result;
    }

    /**
     * The original ItemStack itself, not even a clone.
     */
    public void setResult(@Nullable ItemStack item) {
        result = item;
    }

    /**
     * @return The meta of {@link #getStack()} but without that
     * pesky {@link Nullable} annotation.
     */
    @NotNull
    @Deprecated
    public ItemMeta getMeta() {
        return getStack().getItemMeta();
    }

    /**
     * The player to reroll modifiers based on their level
     */
    @Nullable
    private RPGPlayer player;

    /**
     * @return player The player to reroll modifiers based on their level
     */
    @Nullable
    public RPGPlayer getPlayer() {
        return player;
    }

    /**
     * @param player The player to reroll modifiers based on their level
     */
    @Deprecated
    public void setPlayer(@Nullable Player player) {
        setPlayer(player == null ? null : PlayerData.get(player).getRPG());
    }

    /**
     * @param player The player to reroll modifiers based on their level
     */
    @Deprecated
    public void setPlayer(@Nullable RPGPlayer player) {
        this.player = player;
    }

    /**
     * If the item should update, this wont be null anymore.
     * <p>
     * Guaranteed not-null when updating.
     */
    @Nullable
    LiveMMOItem oldMMOItem;

    /**
     * @return The MMOItem being updated. For safety, it should be cloned,
     * in case any plugin decides to make changes in it... though
     * this should be entirely for <b>reading purposes only</b>.
     */
    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @NotNull
    public LiveMMOItem getOldMMOItem() {
        return oldMMOItem;
    }

    /**
     * The loaded template of the MMOItem in question. If it's null,
     * no template is associated to the item stack passed as argument
     * on instanciation, and reforging is impossible.
     * <p>
     * Guaranteed not-null when updating.
     */
    @Nullable
    private final MMOItemTemplate template;

    /**
     * @return The loaded template of the MMOItem in question.
     */
    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @NotNull
    public MMOItemTemplate getTemplate() {
        return Objects.requireNonNull(template, "No item template was found");
    }

    /**
     * The Updated version of the MMOItem, with
     * its revised stats.
     * <p>
     * Guaranteed not-null when updating.
     */
    @Nullable
    private MMOItem freshMMOItem;

    /**
     * @return The Updated version of the MMOItem, with
     * its revised stats.
     */
    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @NotNull
    public MMOItem getFreshMMOItem() {
        return freshMMOItem;
    }

    /**
     * @param mmo The Updated version of the MMOItem, with
     *            the revised stats.
     */
    public void setFreshMMOItem(@NotNull MMOItem mmo) {
        freshMMOItem = mmo;
    }

    /**
     * @return If this is a loaded template. That's all required.
     * @deprecated Ambigous method, not finding a corresponding item
     * template isn't the only fail factor.
     */
    @Deprecated
    public boolean canReforge() {
        return hasTemplate();
    }

    /**
     * @return If it has found a template corresponding to the item
     */
    public boolean hasTemplate() {
        return template != null;
    }

    /**
     * Sometimes, reforging will take away things from the item that
     * we don't want to be destroyed forever, these items will drop
     * back to the player at the end of the operation.
     * <br><br>
     * One example are gemstones, when the updated object has less
     * gemstone capacity or different color slots, it would be sad
     * if the current gemstones ceased to exist. Instead, when the
     * event runs, the gemstone updater stores the gem items here
     * so that the player gets them back at the completion of this.
     */
    @NotNull
    final ArrayList<ItemStack> reforgingOutput = new ArrayList<>();

    /**
     * Sometimes, reforging will take away things from the item that
     * we don't want to be destroyed forever, these items will drop
     * back to the player at the end of the operation.
     * <br><br>
     * One example are gemstones, when the updated object has less
     * gemstone capacity or different color slots, it would be sad
     * if the current gemstones ceased to exist. Instead, when the
     * event runs, the gemstone updater stores the gem items here
     * so that the player gets them back at the completion of this.
     *
     * @param item Add an item to this process.
     */
    public void addReforgingOutput(@Nullable ItemStack item) {
        // Ew
        if (!SilentNumbers.isAir(item) && item.getType().isItem())
            // Add that
            reforgingOutput.add(item);
    }

    /**
     * Sometimes, reforging will take away things from the item that
     * we don't want to be destroyed forever, these items will drop
     * back to the player at the end of the operation.
     * <br><br>
     * One example are gemstones, when the updated object has less
     * gemstone capacity or different color slots, it would be sad
     * if the current gemstones ceased to exist. Instead, when the
     * event runs, the gemstone updater stores the gem items here
     * so that the player gets them back at the completion of this.
     */
    public void clearReforgingOutput() {
        reforgingOutput.clear();
    }

    /**
     * Sometimes, reforging will take away things from the item that
     * we don't want to be destroyed forever, these items will drop
     * back to the player at the end of the operation.
     * <br><br>
     * One example are gemstones, when the updated object has less
     * gemstone capacity or different color slots, it would be sad
     * if the current gemstones ceased to exist. Instead, when the
     * event runs, the gemstone updater stores the gem items here
     * so that the player gets them back at the completion of this.
     *
     * @return All the items that will be dropped. The list itself.
     */
    @NotNull
    public ArrayList<ItemStack> getReforgingOutput() {
        return reforgingOutput;
    }

    /**
     * The item level modifying the values of RandomStatData
     * upon creating a new MMOItem from the template.
     */
    int generationItemLevel;

    /**
     * @return The item level modifying the values of RandomStatData
     * upon creating a new MMOItem from the template.
     */
    public int getGenerationItemLevel() {
        return generationItemLevel;
    }

    public boolean reforge(@NotNull ReforgeOptions options) {
        return reforge(options, (RPGPlayer) null);
    }

    public boolean reforge(@NotNull ReforgeOptions options, @Nullable Player player) {
        return reforge(options, player == null ? null : PlayerData.get(player).getRPG());
    }

    /**
     * Go through all the modules and build the output item! The output
     * item is cached but subsequent calls will destroy the past result
     * and generate a brand-new one.
     * <p>
     * Due to randomly generated stats this method is non-deterministic.
     *
     * @param options Additional options to pass onto the modules.
     *                These options are independent of the template considered.
     * @param player  The player to reroll modifiers based on their level. It
     *                is not being used by MMOItems but passed as argument in
     *                called Bukkit events.
     * @return If reforged successfully. Basically <code>true</code>, unless cancelled.
     */
    public boolean reforge(@NotNull ReforgeOptions options, @Nullable RPGPlayer player) {

        // Throw fail
        if (!hasTemplate()) return false;

        // Prepare everything properly
        oldMMOItem = new LiveMMOItem(getNBTItem());

        // Not blacklisted right!?
        if (options.isBlacklisted(getOldMMOItem().getId())) return false;

        this.player = player;

        // What level with the regenerated item will be hmmmm.....
        generationItemLevel = (getOldMMOItem().hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) getOldMMOItem().getData(ItemStats.ITEM_LEVEL)).getValue() : 0);

        // Identify tier.
        ItemTier tier =

                // Does the item have a tier, and it should keep it?
                (options.shouldKeepTier() && getOldMMOItem().hasData(ItemStats.TIER)) ?

                        // The tier will be the current tier
                        MMOItems.plugin.getTiers().get(getOldMMOItem().getData(ItemStats.TIER).toString())

                        // The item either has no tier, or shouldn't keep it. Null
                        : null;

        // Build it again (Reroll RNG)
        setFreshMMOItem(getTemplate().newBuilder(generationItemLevel, tier).build());
        //RFG//MMOItems.log("§8Reforge §4RFG§7 Generated at Lvl \u00a73" + generationItemLevel);

        // Run event
        //RFG//MMOItems.log("§8Reforge §3RFG§7 Running Reforge Event");
        MMOItemReforgeEvent mmoREV = new MMOItemReforgeEvent(this, options);
        Bukkit.getPluginManager().callEvent(mmoREV);

        // Cancelled? it ends there
        if (mmoREV.isCancelled()) return false;

        // Properly recalculate all based on histories
        for (StatHistory hist : getFreshMMOItem().getStatHistories())
            // Recalculate that shit
            getFreshMMOItem().setData(hist.getItemStat(), hist.recalculate(getFreshMMOItem().getUpgradeLevel()));

        if (getFreshMMOItem().hasUpgradeTemplate())

            for (ItemStat stat : getFreshMMOItem().getUpgradeTemplate().getKeys()) {

                // That stat yes
                StatHistory hist = StatHistory.from(getFreshMMOItem(), stat);

                // Recalculate that shit
                getFreshMMOItem().setData(hist.getItemStat(), hist.recalculate(getFreshMMOItem().getUpgradeLevel()));
            }

        // Build ItemStack
        result = getFreshMMOItem().newBuilder().build();

        // Run another event...
        MMOItemReforgeFinishEvent mmoFIN = new MMOItemReforgeFinishEvent(result, this, options);
        Bukkit.getPluginManager().callEvent(mmoFIN);

        // Finally, the result item.
        setResult(mmoFIN.getFinishedItem());
        //RFG//MMOItems.log("§8Reforge §6RFG§7 Finished " + SilentNumbers.getItemName(getResult()));

        // That's the result
        return !mmoFIN.isCancelled();
    }

    //region Config Values
    public static boolean keepTiersWhenReroll = true;
    public static boolean gemstonesRevIDWhenUnsocket = false;

    public static void reload() {
        keepTiersWhenReroll = MMOItems.plugin.getConfig().getBoolean("item-revision.keep-tiers");
        gemstonesRevIDWhenUnsocket = MMOItems.plugin.getConfig().getBoolean("item-revision.regenerate-gems-when-unsocketed", false);
    }
    //endregion

    //region Deprecated API
    @Deprecated
    public void update(@Nullable Player p, @NotNull ReforgeOptions options) {
        reforge(options, p);
    }

    @Deprecated
    public void update(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {
        reforge(options, player);
    }

    @Deprecated
    void regenerate(@Nullable RPGPlayer p) {
        reforge(new ReforgeOptions(false, false, false, false, false, false, false, true), p);
    }

    @Deprecated
    int regenerate(@Nullable RPGPlayer player, @NotNull MMOItemTemplate template) {
        reforge(new ReforgeOptions(false, false, false, false, false, false, false, true), player);
        return 0;
    }

    @Deprecated
    public void reforge(@Nullable Player p, @NotNull ReforgeOptions options) {
        reforge(options, p);
    }

    @Deprecated
    public void reforge(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {
        reforge(options, player);
    }

    @Deprecated
    public ItemStack toStack() {
        return getResult();
    }

    @Deprecated
    public boolean hasChanges() {
        return getResult() != null;
    }

    @Deprecated
    @NotNull
    public ArrayList<MMOItem> getDestroyedGems() {
        return new ArrayList<>();
    }

    //endregion
}
