package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.gson.*;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The primordial problem is removing Gem Stones.
 * <p></p>
 * To achieve this, we must know which stat is from the item originally, vs which stats were given to it from each gem stone.
 * We must also account for weapon upgrades and such.
 * <p></p>
 * This class will store the different sources of each stat UPON being modified.
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class StatHistory {

    /*
     * Which stat is this the history of?
     */
    @NotNull
    private final ItemStat itemStat;

    /**
     * Which stat is this the history of?
     */
    @NotNull
    public ItemStat getItemStat() {
        return itemStat;
    }

    /**
     * @return Sure there is a Stat History and all but, does it
     * actually have any information apart from the OG Data?
     */
    public boolean isClear() {

        /*
         * Enchant list data is not clear even if redundant.
         *
         * Its an important assumption in several methods
         * like Enchants.separateEnchantments()
         */
        if (getOriginalData() instanceof EnchantListData) {
            if (((EnchantListData) getOriginalData()).getEnchants().size() != 0) {
                //CLR//MMOItems.log("\u00a7a -+- \u00a77Found Enchantments, \u00a7cnot clear. \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}");
                return false;
            }
        }

        // Any gemstones or external SH? Then its NOT CLEAR
        if (getAllGemstones().size() > 0 || getExternalData().size() > 0 || getAllModifiers().size() > 0) {
            //CLR//MMOItems.log("\u00a7a -+- \u00a77Found Gemstones / ESH, \u00a7cnot clear. \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}");
            return false;
        }

        // Is it clear?
        if (((Mergeable) getOriginalData()).isClear() && (!isUpgradeable() || getMMOItem().getUpgradeLevel() == 0)) {
            //CLR//MMOItems.log("\u00a7a -+- \u00a77Original data is clear & unupgraded, \u00a7aclear. \u00A73(\u00a78Upgradeable? \u00a7b" + isUpgradeable() + "\u00a78, Upgrade Level:\u00a7b " + getMMOItem().getUpgradeLevel() + "\u00a73) \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}");
            return true;
        }

        // Exactly the same as the MMOItem? [This check should basically always be true though]
        //CLR//if (getOriginalData().equals(getMMOItem().getData(getItemStat()))) { MMOItems.log("\u00a7a -+- \u00a77Original data has never been merged, \u00a7aclear. \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}"); }
        return getOriginalData().equals(getMMOItem().getData(getItemStat()));
    }

    /*
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull
    MMOItem parent;

    /**
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull
    public MMOItem getMMOItem() {
        return parent;
    }

    /*
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull
    StatData originalData;

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull
    public StatData getOriginalData() {
        //noinspection ConstantConditions
        if (originalData == null) {
            setOriginalData(getItemStat().getClearStatData());
            MMOItems.print(null, "Stat History for $e{0}$b in $u{1} {2}$b had null original data.", null, getItemStat().getId(), getMMOItem().getType().toString(), getMMOItem().getId());
        }
        return originalData;
    }

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    public void setOriginalData(@NotNull StatData s) {
        originalData = s;
    }

    /**
     * The total bonus given by modifiers that
     * were rolled when the item was first created.
     */
    @NotNull
    public HashMap<UUID, StatData> perModifierBonus = new HashMap<>();

    /**
     * @return The total bonus given by modifiers that
     * were rolled when the item was first created.
     */
    @Contract("null -> null")
    @Nullable
    public StatData getModifiersBonus(@Nullable UUID of) {
        if (of == null) {
            return null;
        }
        return perModifierBonus.get(of);
    }

    /**
     * @param of   UUID to link this modifier to
     * @param data The total bonus given by modifiers that
     *             were rolled when the item was first created.
     */
    public void registerModifierBonus(@NotNull UUID of, @NotNull StatData data) {
        perModifierBonus.put(of, data);
    }

    /**
     * Removes the modifier of such UUID from those registered.
     *
     * @param of UUID of the modifier to remove.
     */
    public void removeModifierBonus(@NotNull UUID of) {
        perModifierBonus.remove(of);
    }

    /**
     * All the Stat Datas provided by Modifiers
     */
    @NotNull
    public ArrayList<UUID> getAllModifiers() {
        return new ArrayList<>(perModifierBonus.keySet());
    }
    /**
     * Clears modifier data. No way to undo so be wary of using.
     */
    public void clearModifiersBonus() { perModifierBonus.clear(); }

    /*
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @NotNull
    public HashMap<UUID, StatData> perGemstoneData = new HashMap<>();
    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @Contract("null -> null")
    @Nullable
    public StatData getGemstoneData(@Nullable UUID of) {
        if (of == null) {
            return null;
        }
        return perGemstoneData.get(of);
    }
    /**
     * Removes the gem of such UUID from those registered.
     *
     * @param of UUID of the gem to remove.
     */
    public void removeGemData(@NotNull UUID of) { perGemstoneData.remove(of); }
    /**
     * All the Stat Datas provided by GemStones
     */
    @NotNull
    public ArrayList<UUID> getAllGemstones() { return new ArrayList<>(perGemstoneData.keySet()); }
    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     * <p></p>
     * Basically, supposing this stands for a double data like <i>Attack Damage</i>:
     * <p>originally <code>+5</code>, now at level 2, with <code>+0.25</code> per level</p>
     * The value of this stat data will be <b><code>+5.5</code></b>
     */
    public void registerGemstoneData(@NotNull UUID of, @NotNull StatData data) { perGemstoneData.put(of, data); }
    /**
     * Clears gemstone data. No way to undo so be wary of using.
     */
    public void clearGemstones() { perGemstoneData.clear(); }

    /*
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     */
    @NotNull
    ArrayList<StatData> perExternalData = new ArrayList<>();

    /**
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     * <p></p>
     * This returns the actual list, so modifying it will modify the 'external data'
     * <p></p>
     * <i>External Data</i> is just a fancy name for '<i>GemStones with no UUID</i>'
     * <p>They act as gem stones, adding together to produce the total of the item, but cannot be removed natively, since there is no way to tell them from each other.</p>
     * Well, I guess whatever plugin is putting them here may remove them by editing the list directly with <code>StatHistory.getExternalData()</code>
     */
    @NotNull
    public ArrayList<StatData> getExternalData() { return perExternalData; }
    /**
     * Collapses all ExSH stat data into one.
     */
    public void consolidateEXSH() {

        // Create Clear
        StatData theEXSH = getItemStat().getClearStatData();

        // Merge All
        for (StatData ex : getExternalData()) {
            if (ex == null) {
                continue;
            }
            ((Mergeable) theEXSH).merge(ex);
        }

        // Clear and Register
        getExternalData().clear();
        registerExternalData(theEXSH);
    }
    /**
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     * <p></p>
     * <i>External Data</i> is just a fancy name for '<i>GemStones with no UUID</i>'
     * <p>They act as gem stones, adding together to produce the total of the item, but cannot be removed, since there is no way to tell them from each other.</p>
     * Well, I guess whatever plugin is putting them here may remove them by editing the list directly with <code>StatHistory.getExternalData()</code>
     */
    public void registerExternalData(@NotNull StatData data) { perExternalData.add(data); }
    /**
     * Clears exsh data. No way to undo so be wary of using.
     */
    public void clearExternalData() { perExternalData.clear(); }

    /**
     * Gets the stat history of this item. <b>The stat must be <code>Mergeable</code></b>
     * <p></p>
     * If the item has no stat history, it will be created anew and appended; the current stat values will become the 'Original' ones,
     * and will be forever unchangeable.
     * <p></p>
     * <b>Make sure the item has the stat present</b>
     *
     * @param ofItem MMOItem to extract stat history from
     * @param ofStat Stat of which to make history
     */
    @NotNull public static StatHistory from(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat) { return from(ofItem, ofStat, false); }
    /**
     * Gets the stat history of this item. <b>The stat must be <code>Mergeable</code></b>
     * <p></p>
     * If the item has no stat history, it will be created anew and appended; the current stat values will become the 'Original' ones,
     * and will be forever unchangeable.
     * <p></p>
     * <b>Make sure the item has the stat present</b>
     *
     * @param ofItem MMOItem to extract stat history from
     * @param ofStat Stat of which to make history
     * @param forceNew <b>Only if you know what you are doing</b>, set to true to not check if the item already has stat history of this stat.
     */
    @NotNull
    public static StatHistory from(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat, boolean forceNew) {
        //LVL//MMOItems.log(" \u00a7d*\u00a77-\u00a7a-\u00a761? \u00a77Lvl: \u00a7b" + ofItem.getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");
        // Get history :B
        StatHistory hist;
        if (!forceNew) {
            hist = ofItem.getStatHistory(ofStat);

            // Found? Thats it
            if (hist != null) {
                //UPGRD//MMOItems.log("Found Stat History of \u00a76" + ofStat.getNBTPath() + "\u00a77 in this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());
                //UPGRD//hist.log();
                return hist; } }

        // That is Mergeable right...
        //UPGRD//MMOItems.log("\u00a7aCreated Hisotry of \u00a76" + ofStat.getNBTPath() + "\u00a7a of this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());
        Validate.isTrue(ofStat.getClearStatData() instanceof Mergeable, "Non-Mergeable stat data wont have a Stat History; they cannot be modified dynamically in the first place.");

        // Get original data
        StatData original = ofItem.getData(ofStat);
        //LVL//MMOItems.log(" \u00a7d*\u00a77-\u00a7a-\u00a762? \u00a77Lvl: \u00a7b" + ofItem.getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");
        if (original == null) {
            original = ofStat.getClearStatData();
            ofItem.setData(ofStat, original);
            //UPGRD//MMOItems.log("\u00a7e   +\u00a77 Item didnt have this stat, original set as blanc.");

        } else {
            original = ((Mergeable) original).cloneData();
            //UPGRD//MMOItems.log("\u00a7a   +\u00a77 Found original data\u00a7f " + original);
        }
        //LVL//MMOItems.log(" \u00a7d*\u00a77-\u00a7a-\u00a763? \u00a77Lvl: \u00a7b" + ofItem.getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");
        
        // Create new
        hist = new StatHistory(ofItem, ofStat, original);

        //LVL//MMOItems.log(" \u00a7d*\u00a77-\u00a7a-\u00a764? \u00a77Lvl: \u00a7b" + ofItem.getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");
        // Append to the item
        ofItem.setStatHistory(ofStat, hist);

        //LVL//MMOItems.log(" \u00a7d*\u00a77-\u00a7a-\u00a765? \u00a77Lvl: \u00a7b" + ofItem.getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");

        //LVL//MMOItems.log(" \u00a7d*\u00a77*\u00a7a*\u00a766? \u00a77Lvl: \u00a7b" + hist.getMMOItem().getUpgradeLevel() + "\u00a7d-\u00a77-\u00a7a-\u00a7d-\u00a77-\u00a7a-");
        // Thats it
        return hist;
    }

    /**
     * Simplemost constructor, shall never be actually called outside this class.
     * <p></p>
     * Use <code>StatHistory.From()</code> to get the stat history associated to an item.
     */
    public StatHistory(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat, @NotNull StatData ogData) { itemStat = ofStat; originalData = ogData; parent = ofItem; }

    /**
     * Checks the item and makes sure that the UUIDs
     * attributed to gemstones link to existing gem
     * stones. Removes them if no such gemstone exists.
     */
    public void purgeGemstones() {

        // Which will get purged...
        ArrayList<UUID> extraneous = new ArrayList<>();
        GemSocketsData data = (GemSocketsData) getMMOItem().getData(ItemStats.GEM_SOCKETS);
        if (data == null) { data = new GemSocketsData(new ArrayList<>()); }

        // For each UUID
        for (UUID gem : perGemstoneData.keySet()) {

            // Check Gemstones
            boolean success = false;
            for (GemstoneData indiv : data.getGemstones()) {

                // Not null
                if (indiv != null) {

                    // Equal in UUID
                    if (gem.equals(indiv.getHistoricUUID())) {

                        success = true;
                        break;
                    }
                }
            }

            // No success?
            if (!success) {

                // No gemstone matched
                extraneous.add(gem);
            }
        }

        // Unregister
        for (UUID ext : extraneous) {
            //RECALCULATE//MMOItems.log("\u00a76 ||\u00a77 Purged Stone: \u00a7e" + ext.toString() + "\u00a78 (\u00a73" + getItemStat().getId() + "\u00a78)");

            // Remove
            removeGemData(ext);
        }
    }

    /**
     * @return If this stat changes when the MMOItem is upgraded.
     */
    public boolean isUpgradeable() {

        // No upgrades no possible
        if (!getMMOItem().hasUpgradeTemplate()) { return false; }

        // Get Upgrade Info?
        UpgradeInfo inf = getMMOItem().getUpgradeTemplate().getUpgradeInfo(getItemStat());

        // No Upgrade Information? Looks like you're calculating as a normal merge stat
        return inf != null;
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * This will not apply the changes, it will just give you the final
     * <code>StatData</code> that shall be applied (used when upgrading).
     */
    @NotNull public StatData recalculate(int level) { return recalculate(true, level); }
    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * This will not apply the changes, it will just give you the final
     * <code>StatData</code> that shall be applied (used when upgrading).
     * @param withPurge Check if the gemstones UUIDs are valid.
     *                  Leave <code>true</code> unless you know
     *                  what you're doing.
     */
    @NotNull public StatData recalculate(boolean withPurge, int level) {
        //RECALCULATE//MMOItems.log("\u00a7d|||\u00a77 Recalculating \u00a7f" + getItemStat().getNBTPath() + "\u00a77, Purge? \u00a7e" + withPurge);

        if (withPurge) { purgeGemstones(); }

        // If its upgradeable and not level ZERO, it must apply upgrades
        //UPGRD//MMOItems.log("\u00a7d|\u00a79|\u00a76|\u00a77 Upgradeable Requirements: ");
        //UPGRD//MMOItems.log("    \u00a76|\u00a77 Upgrade Level: \u00a7e" + level);
        //UPGRD//MMOItems.log("    \u00a76|\u00a77 Upgradeable Stat: \u00a7e" + (getItemStat() instanceof Upgradable));
        //UPGRD//MMOItems.log("    \u00a76|\u00a77 Template Exists: \u00a7e" + (getMMOItem().hasUpgradeTemplate()));
        if ((level != 0)  &&
            (getItemStat() instanceof Upgradable) &&
            (getMMOItem().hasUpgradeTemplate())) { 
            
            // Recalculate upgrading
            return recalculateUpgradeable(level);
        } 
        
        // Merge Normally
        return recalculateMergeable();
    }

    /**
     * This recalculates values accounting only for gemstones and external data.
     * <p></p>
     * In case someone was wondered the contribution of upgrading the item, just
     * substract it from {@link #recalculate(int)}
     */
    @NotNull public StatData recalculateUnupgraded() { return recalculateUnupgraded(true); }

    /**
     * This recalculates values accounting only for gemstones and external data.
     * <p></p>
     * In case someone was wondered the contribution of upgrading the item, just
     * substract it from {@link #recalculate(int)}
     * @param withPurge Check if the gemstones UUIDs are valid.
     *                  Leave <code>true</code> unless you know
     *                  what you're doing.
     */
    @NotNull public StatData recalculateUnupgraded(boolean withPurge) {
        if (withPurge) { purgeGemstones(); }

        // Merge Normally
        return recalculateMergeable();
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * That is, it (in this order):
     * <p>1: Starts out with a fresh (empty) data
     * </p>2: Sums the original values
     * <p>3: Scales to current Upgrade Level
     * </p>4: Sums Gem Stone Data (which should be scaled accordingly [Upgrades are entirely merged into their data])
     * <p>5: Sums external data (modifiers that are not linked to an ID, I suppose by external plugins).
     */
    private StatData recalculateUpgradeable(int lvl) {
        //RECALCULATE//MMOItems.log("\u00a76|||\u00a77 Calculating \u00a7f" + getItemStat().getNBTPath() + "\u00a77 as Upgradeable");

        // Get Upgrade Info?
        UpgradeInfo inf = getMMOItem().getUpgradeTemplate().getUpgradeInfo(getItemStat());

        // No Upgrade Information? Looks like you're calculating as a normal merge stat
        if (inf == null) { return recalculateMergeable(); }

        // Clone original
        StatData ogCloned = ((Mergeable) originalData).cloneData();
        //DBL//if (ogCloned instanceof DoubleData) MMOItems.log("\u00a76  >\u00a77 Original Base: \u00a7e" + ((DoubleData) ogCloned).getValue() + "\u00a78 {Original:\u00a77 " + ((DoubleData) getOriginalData()).getValue() + "\u00a78}");

        // Add Modifiers (who are affected by upgrades as if they was the base item data
        for (UUID d : perModifierBonus.keySet()) {

            //DBL//if (getModifiersBonus() instanceof DoubleData) MMOItems.log("\u00a76  >\u00a7c> \u00a77 Modifier Base: \u00a7e" + ((DoubleData) getModifiersBonus()).getValue());
            // Just merge ig
            ((Mergeable) ogCloned).merge(((Mergeable) getModifiersBonus(d)).cloneData());
        }

        // Level up
        //RECALCULATE//MMOItems.log("\u00a76 ||\u00a77 Item Level: \u00a7e" + lvl);
        StatData ret = ((Upgradable) getItemStat()).apply(ogCloned, inf, lvl);
        //DBL//if (ret instanceof DoubleData) MMOItems.log("\u00a76  >\u00a77 Leveled Base: \u00a7e" + ((DoubleData) ret).getValue() + "\u00a78 {Original:\u00a77 " + ((DoubleData) getOriginalData()).getValue() + "\u00a78}");

        // Add up gemstones
        for (UUID d : perGemstoneData.keySet()) {

            // Identify insertion level (When was the gemstone put into the item?
            int level = 0;

            // Whats this gemstone's upgrade level?
            for (GemstoneData gData : getMMOItem().getGemStones()) {
                if (gData == null) { continue; }
                //RECALCULATE//MMOItems.log("\u00a76 -\u00a7b-\u00a76-\u00a77 Gemstone " + gData.getName() + "\u00a77 " + gData.getHistoricUUID().toString());

                // Find that one of matching UUID
                if (gData.getHistoricUUID().equals(d)) {

                    if (gData.isScaling()) {

                        // Ok
                        level = gData.getLevel();
                        //RECALCULATE//MMOItems.log("\u00a76 -\u00a7b-\u00a76-\u00a7a- Found:\u00a77" + level);

                    } else {

                        // No scaling
                        level = lvl;
                        //RECALCULATE//MMOItems.log("\u00a76 -\u00a7b-\u00a76-\u00a7a- Found,\u00a77 Unscaling");
                    }
                }
            }

            // Calculate level difference
            int gLevel = lvl - level;
            //RECALCULATE//MMOItems.log("\u00a76 |\u00a7b|\u00a76>\u00a77 Gemstone Level: \u00a7e" + gLevel + "\u00a77 (Put at \u00a7b" + level + "\u00a77)");

            //DBL//if (getGemstoneData(d) instanceof DoubleData) MMOItems.log("\u00a76  \u00a7b|>\u00a77 Gemstone Base: \u00a7e" + ((DoubleData) getGemstoneData(d)).getValue());
            // Apply upgrades
            //noinspection ConstantConditions
            StatData gRet = ((Upgradable) getItemStat()).apply(((Mergeable) getGemstoneData(d)).cloneData(), inf, gLevel);
            //DBL//if (gRet instanceof DoubleData) MMOItems.log("\u00a76  \u00a7b|>\u00a77 Leveled Base: \u00a7e" + ((DoubleData) gRet).getValue());

            // Merge
            ((Mergeable) ret).merge(((Mergeable) gRet).cloneData());
        }

        // Add up externals (who dont suffer upgrades
        for (StatData d : getExternalData()) {

            //DBL//if (d instanceof DoubleData) MMOItems.log("\u00a76  >\u00a7c> \u00a77 Extraneous Base: \u00a7e" + ((DoubleData) d).getValue());
            // Just merge ig
            ((Mergeable) ret).merge(((Mergeable) d).cloneData());
        }

        // Return result
        //DBL//if (ret instanceof DoubleData) MMOItems.log("\u00a76:::\u00a77 Result: \u00a7e" + ((DoubleData) ret).getValue() + "\u00a78 {Original:\u00a77 " + ((DoubleData) getOriginalData()).getValue() + "\u00a78}");
        return ret;
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * That is, it (in this order):
     * <p>1: Starts out with a fresh (empty) data
     * </p>2: Sums the original values
     * </p>3: Sums Gem Stone Data (which should be scaled accordingly [Upgrades are entirely merged into their data])
     * <p>4: Sums external data (modifiers that are not linked to an ID, I suppose by external plugins).
     */
    private StatData recalculateMergeable() {
        //RECALCULATE//MMOItems.log("\u00a73|||\u00a77 Calculating \u00a7f" + getItemStat().getNBTPath() + "\u00a77 as Mergeable");

        // Just clone bro
        StatData ret =  ((Mergeable) getOriginalData()).cloneData();

        //DBL//if (ret instanceof DoubleData) MMOItems.log("\u00a73  > \u00a77 Original Base: \u00a7e" + ((DoubleData) ret).getValue());
        
        // Add Modifiers
        for (StatData d : perModifierBonus.values()) {
            //DBL//if (getModifiersBonus() instanceof DoubleData) MMOItems.log("\u00a73  >\u00a7c> \u00a77 Modifier Base: \u00a7e" + ((DoubleData) getModifiersBonus()).getValue());
            // Just merge ig
            ((Mergeable) ret).merge(((Mergeable) d).cloneData());
        }

        // Add up gemstones
        for (StatData d : perGemstoneData.values()) {
            //DBL//if (d instanceof DoubleData) MMOItems.log("\u00a73  >\u00a7b> \u00a77 Gemstone Base: \u00a7e" + ((DoubleData) d).getValue());
            ((Mergeable) ret).merge(((Mergeable) d).cloneData());
        }

        // Add up externals
        for (StatData d : getExternalData()) {
            //DBL//if (d instanceof DoubleData) MMOItems.log("\u00a73  >\u00a7c> \u00a77 Extraneous Base: \u00a7e" + ((DoubleData) d).getValue());
            ((Mergeable) ret).merge(((Mergeable) d).cloneData()); }

        // Return result
        //DBL//if (ret instanceof DoubleData) MMOItems.log("\u00a73:::\u00a77 Result: \u00a7b" + ((DoubleData) ret).getValue());
        return ret;
    }

    /**
     * To store onto the NBT of the item.
     * <p></p>
     * I've heard its not very optimized, but honestly that just means that
     * instead of running in 0.0001s it runs in 0.0002s idk.
     * <p></p>
     * Still don't abuse calls to this. Try to do so only when necessary
     */
    @NotNull public JsonObject toJson() {
        JsonObject object = new JsonObject();

        // To know the stat it was
        object.addProperty(enc_Stat, getItemStat().getId());

        /*
         * Save the original data. It is redundant to save if it is clear though.
         *
         * Except if it is the ENCHANTS ItemStat, in which a clear data must be saved
         * so that we can know which enchantments came from vanilla sources (like the
         * enchantment table) instead of being born with the MMOItem template.
         *
         * This is an assumption made by net.Indyuce.mmoitems.stat.Enchants.separateEnchantments()
         * And allows net.Indyuce.mmoitems.stat.Enchants.whenLoaded() to correctly initialize the
         * StatHistory of these items.
         */
        if (!((Mergeable) getOriginalData()).isClear() || getItemStat() == ItemStats.ENCHANTS) { object.add(enc_OGS, ItemTag.compressTags(getItemStat().getAppliedNBT(getOriginalData()))); }

        // Kompress Arrays
        JsonArray gemz = new JsonArray();

        // Compress I suppose
        for (UUID gem : getAllGemstones()) {

            // As Json Object
            JsonObject yes = new JsonObject();

            // Compress tags
            //noinspection ConstantConditions
            JsonArray yesCompressed = ItemTag.compressTags(getItemStat().getAppliedNBT(getGemstoneData(gem)));

            // Put
            yes.add(gem.toString(), yesCompressed);

            // Actually Include
            gemz.add(yes);
        }

        // Include
        if (gemz.size() > 0) { object.add(enc_GSS, gemz); }


        // Kompress Arrays
        JsonArray externals = new JsonArray();

        // Compress I suppose
        for (StatData ex : getExternalData()) {

            // Skip clear
            if (((Mergeable) ex).isClear()) { continue; }

            // Put
            externals.add(ItemTag.compressTags(getItemStat().getAppliedNBT(ex)));
        }

        // Include
        if (externals.size() > 0) { object.add(enc_EXS, externals); }

        // Kompress Arrays
        JsonArray modz = new JsonArray();

        // Original data
        for (UUID mod : getAllModifiers()) {

            // As Json Object
            JsonObject yes = new JsonObject();

            // Compress tags
            //noinspection ConstantConditions
            JsonArray yesCompressed = ItemTag.compressTags(getItemStat().getAppliedNBT(getModifiersBonus(mod)));

            // Put
            yes.add(mod.toString(), yesCompressed);

            // Actually Include
            modz.add(yes);
        }

        // Include
        if (modz.size() > 0) { object.add(enc_MOD, modz); }


        return object;
    }

    /**
     * To store onto the NBT of the item.
     * <p></p>
     * I've heard its not very optimized, but honestly that just means that
     * instead of running in 0.0001s it runs in 0.0002s idk.
     * <p></p>
     * Still don't abuse calls to this. Try to do so only when necessary
     */
    @NotNull public String toNBTString() {

        // Just convert to string :thinking:
        return toJson().toString();
    }

    /**
     * To read from NBT data. This undoes {@link #toJson()} basically.
     * <p></p>
     * @param iSource The MMOItem you are trying to read the NBT of
     */
    @Nullable public static StatHistory fromJson(@NotNull MMOItem iSource, @NotNull JsonObject json) {

        // Get the stat we're searching for
        JsonElement statEncode;
        JsonElement ogStatsEncode= null;
        JsonElement gemsEncode = null;
        JsonElement extEncode = null;
        JsonElement modEncode = null;

        // It has stat information right?
        if (json.has(enc_Stat)) { statEncode = json.get(enc_Stat); } else { return null; }
        if (json.has(enc_OGS)) { ogStatsEncode = json.get(enc_OGS); }
        if (json.has(enc_GSS)) { gemsEncode = json.get(enc_GSS); }
        if (json.has(enc_EXS)) { extEncode = json.get(enc_EXS); }
        if (json.has(enc_MOD)) { modEncode = json.get(enc_MOD); }

        // It is a primitive right
        if (!statEncode.isJsonPrimitive()) { return null; }
        if (ogStatsEncode != null && !ogStatsEncode.isJsonArray()) { return null; }
        if (gemsEncode != null && !gemsEncode.isJsonArray()) { return null; }
        if (extEncode != null && !extEncode.isJsonArray()) { return null; }
        if (modEncode != null && !modEncode.isJsonArray()) { return null; }

        // Get string
        String statInternalName = statEncode.getAsJsonPrimitive().getAsString();

        // Get stat
        ItemStat stat = MMOItems.plugin.getStats().get(statInternalName);

        // Nope
        if (stat == null) { return null; }

        // To know the stat it was
        StatData sData;
        if (ogStatsEncode != null) {

            // Decompress tags
            ArrayList<ItemTag> ogDecoded = ItemTag.decompressTags(ogStatsEncode.getAsJsonArray());
            sData = stat.getLoadedNBT(ogDecoded);

        // OG Not included (because its clear)
        } else {

            // Just generate as clear
            sData = stat.getClearStatData();
        }

        // Validate non null
        if (sData == null) { return null; }

        // Can now generate stat history
        StatHistory sHistory = new StatHistory(iSource, stat, sData);

        //region Getting Gem Stone History
        if (gemsEncode != null) {

            // Decompress gems
            for (JsonElement elmnt : gemsEncode.getAsJsonArray()) {

                // Must be an object
                if (elmnt.isJsonObject()) {

                    // Get as Object
                    JsonObject element = elmnt.getAsJsonObject();

                    // Get map
                    Set<Map.Entry<String, JsonElement>> contained = element.entrySet();

                    // There should be exacly one but anyway;
                    for (Map.Entry<String, JsonElement> entry : contained) {

                        // Get path (Gemstone UUID)
                        String gemUUID = entry.getKey();

                        // Attempt to parse gemuuid
                        UUID actualUUID = MMOUtils.UUIDFromString(gemUUID);

                        // Get Stat compressed tag
                        JsonElement compressedTags = entry.getValue();

                        // Succeed?
                        if (compressedTags.isJsonArray() && actualUUID != null) {

                            // Continue...
                            ArrayList<ItemTag> tags = ItemTag.decompressTags(compressedTags.getAsJsonArray());

                            // Generate data
                            StatData gemData = stat.getLoadedNBT(tags);

                            // Validated?
                            if (gemData != null) {

                                // Add
                                sHistory.registerGemstoneData(actualUUID, gemData);
                            }
                        }
                    }
                }
            }
        }
        //endregion

        //region External Stat History
        if (extEncode != null) {

            // Decompress gems
            for (JsonElement elmnt : extEncode.getAsJsonArray()) {

                // Must be an array (compressed tags)
                if (elmnt.isJsonArray()) {

                    // Continue...
                    ArrayList<ItemTag> tags = ItemTag.decompressTags(elmnt.getAsJsonArray());

                    // Generate data
                    StatData exData = stat.getLoadedNBT(tags);

                    // Validated?
                    if (exData != null) {

                        // Add
                        sHistory.registerExternalData(exData);
                    }
                }
            }
        }
        //endregion
        
        //region Modifier History
        if (modEncode != null) {

            // Decompress gems
            for (JsonElement elmnt : modEncode.getAsJsonArray()) {

                // Must be an object
                if (elmnt.isJsonObject()) {

                    // Get as Object
                    JsonObject element = elmnt.getAsJsonObject();

                    // Get map
                    Set<Map.Entry<String, JsonElement>> contained = element.entrySet();

                    // There should be exacly one but anyway;
                    for (Map.Entry<String, JsonElement> entry : contained) {

                        // Get path (Gemstone UUID)
                        String modUUID = entry.getKey();

                        // Attempt to parse gemuuid
                        UUID actualUUID = MMOUtils.UUIDFromString(modUUID);

                        // Get Stat compressed tag
                        JsonElement compressedTags = entry.getValue();

                        // Succeed?
                        if (compressedTags.isJsonArray() && actualUUID != null) {

                            // Continue...
                            ArrayList<ItemTag> tags = ItemTag.decompressTags(compressedTags.getAsJsonArray());

                            // Generate data
                            StatData modData = stat.getLoadedNBT(tags);

                            // Validated?
                            if (modData != null) {

                                // Add
                                sHistory.registerModifierBonus(actualUUID, modData);
                            }
                        }
                    }
                }
            }
        }
        //endregion

        return sHistory;
    }

    /**
     * To read from NBT data. This reverses {@link #toNBTString()} basically.
     * <p></p>
     * Will be null if some error happens
     */
    @Nullable public static StatHistory fromNBTString(@NotNull MMOItem iSource, @NotNull String codedJson) {

        // Attempt
        try {

            // Make JSON Parser
            JsonParser pJSON = new JsonParser();

            // Parse as array
            JsonObject oJSON = pJSON.parse(codedJson).getAsJsonObject();

            // Bake
            return fromJson(iSource, oJSON);

        } catch (Throwable e) {

            // Feedbacc
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
            ffp.activatePrefix(true, "Stat History");
            ffp.log(FriendlyFeedbackCategory.ERROR, "Could not get stat history: $f{0}$b at $f{1}", e.getMessage(), e.getStackTrace()[0].toString());
            ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
            return null;
        }
    }

    /**
     * Get all gemstone and extraneous data from this other, while
     * keeping the current ones as well as <u>these</u> original bases.
     * <p></p>
     * Fails if the stats are not the same one.
     */
    public void assimilate(@NotNull StatHistory other) {

        // Stat must be the same
        if (other.getItemStat().getNBTPath().equals(getItemStat().getNBTPath())) {
           //UPDT//MMOItems.log("    \u00a72>\u00a76> \u00a77History Stat Matches");

           //UPDT//MMOItems.log("     \u00a76:\u00a72: \u00a77Original Gemstones \u00a7f" + perGemstoneData.size());
           //UPDT//MMOItems.log("     \u00a76:\u00a72: \u00a77Original Externals \u00a7f" + perExternalData.size());

            // Register gemstones
            for (UUID exUID : other.getAllGemstones()) {
                //noinspection ConstantConditions
                registerGemstoneData(exUID, other.getGemstoneData(exUID)); }

            // Register externals
            for (StatData ex : other.getExternalData()) { registerExternalData((ex)); }

            // Register modifiers
            for (UUID exUID : other.getAllModifiers()) {
                //noinspection ConstantConditions
                registerModifierBonus(exUID, other.getModifiersBonus(exUID)); }

           //UPDT//MMOItems.log("     \u00a76:\u00a72: \u00a77Final Gemstones \u00a7f" + perGemstoneData.size());
           //UPDT//MMOItems.log("     \u00a76:\u00a72: \u00a77Final Externals \u00a7f" + perExternalData.size());
           //ASS//MMOItems.log("     \u00a76:\u00a72: \u00a77Assimiliaton Result \u00a7f");
           //ASS//log();
        }
    }

    /**
     * Clones this history but linked to another MMOItem.
     *
     * It does not put it into the MMOItem, you must do that yourself.
     *
     * @see MMOItem#setStatHistory(ItemStat, StatHistory)
     *
     * @param clonedMMOItem Usually this is called when you are cloning the MMOItem itself,
     *                      this is a reference to the new one.
     */
    public StatHistory clone(@NotNull MMOItem clonedMMOItem) {

        // Clone
        StatHistory res = new StatHistory(clonedMMOItem, getItemStat(), ((Mergeable) getOriginalData()).cloneData());

        // Add all
        for (UUID uid : getAllGemstones()) {
            if (uid == null) { continue; }

            StatData gem = getGemstoneData(uid);
            if (!(gem instanceof Mergeable)) { continue; }

            // Clone
            res.registerGemstoneData(uid, ((Mergeable) gem).cloneData());
        }

        // Add all
        for (StatData ex : getExternalData()) {
            if (!(ex instanceof Mergeable)) { continue; }

            // Clone
            res.registerExternalData(((Mergeable) ex).cloneData()); }

        // Clone
        for (UUID uid : getAllModifiers()) {

            if (uid == null) { continue; }

            StatData mod = getModifiersBonus(uid);
            if (!(mod instanceof Mergeable)) { continue; }

            // Clone
            res.registerModifierBonus(uid, ((Mergeable) mod).cloneData());
        }

        // Thats it
        return res;
    }

    static final String enc_Stat = "Stat";
    static final String enc_OGS = "OGStory";
    static final String enc_GSS = "Gemstory";
    static final String enc_EXS = "Exstory";
    static final String enc_MOD = "Mod";


    /**
     * Logs into the console. Dev Mehtod
     */
    public void log() {

        MMOItems.print(null, "\u00a76SH of \u00a7e" + getItemStat().getId() + "\u00a77, \u00a7b" + getMMOItem().getType() + " " + getMMOItem().getId(), null);

        if (getOriginalData() instanceof StringListData) {

            MMOItems.print(null, "\u00a7a++ Original", null);
            for (String str : ((StringListData) getOriginalData()).getList()) { MMOItems.print(null, "\u00a7a ++\u00a77 " + str, null); }

            MMOItems.print(null, "\u00a7e++ Gemstones", null);
            for (UUID ui : getAllGemstones()) { StatData sd = getGemstoneData(ui); if (!(sd instanceof StringListData)) { continue; } for (String str : ((StringListData) sd).getList()) { MMOItems.print(null, "\u00a7e ++\u00a77 " + str, null); } }

            MMOItems.print(null, "\u00a7c++ ExSH", null);
            for (StatData sd : getExternalData()) { if (!(sd instanceof StringListData)) { continue; } for (String str : ((StringListData) sd).getList()) { MMOItems.print(null, "\u00a7e ++\u00a77 " + str, null); } }

            MMOItems.print(null, "\u00a7d++ Modifiers", null);
            for (UUID ui : getAllModifiers()) { StatData sd = getModifiersBonus(ui); if (!(sd instanceof StringListData)) { continue; } for (String str : ((StringListData) sd).getList()) { MMOItems.print(null, "\u00a7d ++\u00a77 " + str, null); } }
        } else {

            MMOItems.print(null, "\u00a7a-- Original", null);
            MMOItems.print(null, "\u00a7a ++\u00a77 " + getOriginalData(), null);

            MMOItems.print(null, "\u00a7e-- Gemstones", null);
            for (UUID ui : getAllGemstones()) { StatData sd = getGemstoneData(ui); if (sd == null) { continue; } MMOItems.print(null, "\u00a7e ++\u00a77 " + sd, null);}

            MMOItems.print(null, "\u00a7c-- ExSH", null);
            for (StatData sd : getExternalData()) { if (sd == null) { continue; } MMOItems.print(null, "\u00a7e ++\u00a77 " + sd, null); }

            MMOItems.print(null, "\u00a7d-- Modifiers", null);
            for (UUID ui : getAllModifiers()) { StatData sd = getModifiersBonus(ui); if (sd == null) { continue; } MMOItems.print(null, "\u00a7d ++\u00a77 " + sd, null);}
        }
    }
}
