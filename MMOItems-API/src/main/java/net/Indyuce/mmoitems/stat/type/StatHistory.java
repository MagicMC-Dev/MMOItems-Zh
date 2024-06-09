package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.gson.JsonArray;
import io.lumine.mythic.lib.gson.JsonElement;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.gson.JsonParser;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The primordial problem is removing Gem Stones.
 * <p></p>
 * To achieve this, we must know which stat is from the item originally,
 * vs which stats were given to it from each gem stone. We must also
 * account for weapon upgrades and such.
 * <p></p>
 * This class will store the different sources of each stat UPON being modified.
 */
public class StatHistory {
    private final ItemStat itemStat;
    private StatData originalData;
    private MMOItem parent;

    private HashMap<UUID, StatData> perModifierBonus = new HashMap<>();
    private ArrayList<StatData> perExternalData = new ArrayList<>();
    private HashMap<UUID, StatData> perGemstoneData = new HashMap<>();

    public StatHistory(@NotNull MMOItem parentItem, @NotNull ItemStat parentStat, @NotNull StatData parentData) {
        itemStat = parentStat;
        originalData = parentData;
        parent = parentItem;
    }

    /**
     * Which stat is this the history of?
     */
    @NotNull
    public ItemStat getItemStat() {
        return itemStat;
    }

    /**
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull
    public MMOItem getMMOItem() {
        return parent;
    }

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull
    public StatData getOriginalData() {
        return originalData;
    }

    /**
     * @return Sure there is a Stat History and all but, does it
     * actually have any information apart from the OG Data?
     */
    public boolean isEmpty() {

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
        if (getOriginalData().isEmpty() && (!isUpgradeable() || getMMOItem().getUpgradeLevel() == 0)) {
            //CLR//MMOItems.log("\u00a7a -+- \u00a77Original data is clear & unupgraded, \u00a7aclear. \u00A73(\u00a78Upgradeable? \u00a7b" + isUpgradeable() + "\u00a78, Upgrade Level:\u00a7b " + getMMOItem().getUpgradeLevel() + "\u00a73) \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}");
            return true;
        }

        // Exactly the same as the MMOItem? [This check should basically always be true though]
        //CLR//if (getOriginalData().equals(getMMOItem().getData(getItemStat()))) { MMOItems.log("\u00a7a -+- \u00a77Original data has never been merged, \u00a7aclear. \u00a78{\u00a77" + getItemStat().getId() + "\u00a78}"); }
        return getOriginalData().equals(getMMOItem().getData(getItemStat()));
    }

    @NotNull
    public StatHistory setParent(@NotNull MMOItem parent) {
        this.parent = parent;
        return this;
    }

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    public void setOriginalData(@NotNull StatData s) {
        Validate.notNull(s, "Original data cannot be null");
        originalData = s;
    }

    /**
     * @return The total bonus given by modifiers that
     * were rolled when the item was first created.
     */
    @Nullable
    public StatData getModifiersBonus(@NotNull UUID of) {
        return perModifierBonus.get(of);
    }

    /**
     * @param of   UUID to link this modifier to
     * @param data The total bonus given by modifiers that
     *             were rolled when the item was first created.
     */
    public void registerModifierBonus(@NotNull UUID of, @NotNull StatData data) {
        Validate.notNull(of, "Modifier UUID cannot be null");
        Validate.notNull(data, "Stat data cannot be null");
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
    public void clearModifiersBonus() {
        perModifierBonus.clear();
    }

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @Nullable
    public StatData getGemstoneData(@NotNull UUID of) {
        return perGemstoneData.get(of);
    }

    /**
     * Removes the gem of such UUID from those registered.
     *
     * @param of UUID of the gem to remove.
     */
    public void removeGemData(@NotNull UUID of) {
        perGemstoneData.remove(of);
    }

    /**
     * All the Stat Datas provided by GemStones
     */
    @NotNull
    public ArrayList<UUID> getAllGemstones() {
        return new ArrayList<>(perGemstoneData.keySet());
    }

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     * <p></p>
     * Basically, supposing this stands for a double data like <i>Attack Damage</i>:
     * <p>originally <code>+5</code>, now at level 2, with <code>+0.25</code> per level</p>
     * The value of this stat data will be <b><code>+5.5</code></b>
     */
    public void registerGemstoneData(@NotNull UUID of, @NotNull StatData data) {
        Validate.notNull(of, "Gemstone ID cannot be null");
        Validate.notNull(data, "Stat data cannot be null");
        perGemstoneData.put(of, data);
    }

    /**
     * Clears gemstone data. No way to undo so be wary of using.
     */
    public void clearGemstones() {
        perGemstoneData.clear();
    }

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
    public ArrayList<StatData> getExternalData() {
        return perExternalData;
    }

    /**
     * Merges all external stat datas of unknown
     * origin into one stat data instance.
     */
    public void fuseExternalData() {

        // Create Clear
        StatData theEXSH = getItemStat().getClearStatData();
        for (StatData ex : getExternalData()) ((Mergeable) theEXSH).mergeWith((Mergeable) ex);

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
    public void registerExternalData(@NotNull StatData data) {
        Validate.notNull(data, "Stat data cannot be null");
        perExternalData.add(data);
    }

    /**
     * Clears exsh data. No way to undo so be wary of using.
     */
    public void clearExternalData() {
        perExternalData.clear();
    }

    /**
     * Checks the item and makes sure that the UUIDs attributed to gemstones
     * link to existing gemstones. Removes them if no such gemstone exists.
     */
    public void purgeGemstones() {

        // No socket history can be found => clear all gems
        GemSocketsData data = (GemSocketsData) getMMOItem().getData(ItemStats.GEM_SOCKETS);
        if (data == null) {
            perGemstoneData.clear();
            return;
        }

        // Pb: intersecting between List and HashTable
        HashMap<UUID, StatData> newPerGemstoneData = new HashMap<>();
        for (GemstoneData gemData : data.getGemstones()) {
            final StatData found = perGemstoneData.get(gemData.getHistoricUUID());
            if (found != null) newPerGemstoneData.put(gemData.getHistoricUUID(), found);
        }
        perGemstoneData = newPerGemstoneData;
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * This will not apply the changes, it will just give you the final
     * <code>StatData</code> that shall be applied (used when upgrading).
     */
    @NotNull
    public StatData recalculate(int level) {
        return recalculate(true, level);
    }

    /**
     * This recalculates values accounting only for gemstones and external data.
     * In case someone was wondered the contribution of upgrading the item, just
     * subtract it from {@link #recalculate(int)}.
     */
    @NotNull
    public StatData recalculateUnupgraded() {
        return recalculate(true, null);
    }

    private int findLevel(int upgradeLevel, UUID gemstoneId) {
        for (GemstoneData gemstone : getMMOItem().getGemstones())
            if (gemstone.getHistoricUUID().equals(gemstoneId))
                return gemstone.isScaling() ? gemstone.getLevel() : upgradeLevel;
        throw new IllegalArgumentException("Could not find level of gem " + gemstoneId);
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
    @NotNull
    public StatData recalculate(boolean purgeFirst, @Nullable Integer upgradeLevel) {
        if (purgeFirst) purgeGemstones();

        final UpgradeInfo upgradeInfo = upgradeLevel != null &&
                upgradeLevel != 0 &&
                getItemStat() instanceof Upgradable ?
                getMMOItem().getUpgradeTemplate().getUpgradeInfo(getItemStat()) : null;

        // Clone original
        Mergeable finalData = ((Mergeable) originalData).clone();

        // Add modifiers (affected by upgrades as if they were base item data)
        for (StatData data : perModifierBonus.values()) finalData.mergeWith((Mergeable) data);

        // Level up
        if (upgradeInfo != null)
            finalData = (Mergeable) ((Upgradable) getItemStat()).apply(finalData, upgradeInfo, upgradeLevel);

        // Add up gemstones
        for (UUID gemstoneId : perGemstoneData.keySet()) {
            Mergeable gsData = (Mergeable) getGemstoneData(gemstoneId);
            if (upgradeInfo != null) {
                int levelDifference = upgradeLevel - findLevel(upgradeLevel, gemstoneId);
                gsData = (Mergeable) ((Upgradable) getItemStat()).apply(gsData.clone(), upgradeInfo, levelDifference);
            }
            finalData.mergeWith(gsData);
        }

        // Add up externals (who don't suffer upgrades)
        for (StatData externalData : getExternalData()) finalData.mergeWith((Mergeable) externalData);

        return finalData;
    }

    /**
     * To store onto the NBT of the item.
     * <p></p>
     * I've heard its not very optimized, but honestly that just means that
     * instead of running in 0.0001s it runs in 0.0002s idk.
     * <p></p>
     * Still don't abuse calls to this. Try to do so only when necessary
     */
    @NotNull
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        // To know the stat it was
        object.addProperty(ENC_STAT, getItemStat().getId());

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
        if (!getOriginalData().isEmpty() || getItemStat() == ItemStats.ENCHANTS) {
            object.add(ENC_OGS, ItemTag.compressTags(getItemStat().getAppliedNBT(getOriginalData())));
        }

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
        if (gemz.size() > 0) {
            object.add(ENC_GSS, gemz);
        }


        // Kompress Arrays
        JsonArray externals = new JsonArray();

        // Compress I suppose
        for (StatData ex : getExternalData()) {

            // Skip clear
            if (ex.isEmpty()) {
                continue;
            }

            // Put
            externals.add(ItemTag.compressTags(getItemStat().getAppliedNBT(ex)));
        }

        // Include
        if (!externals.isEmpty()) {
            object.add(ENC_EXS, externals);
        }

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
        if (modz.size() > 0) {
            object.add(ENC_MOD, modz);
        }


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
    @NotNull
    public String toNBTString() {

        // Just convert to string :thinking:
        return toJson().toString();
    }

    public boolean isUpgradeable() {
        return getMMOItem().hasUpgradeTemplate() && getMMOItem().getUpgradeTemplate().getUpgradeInfo(getItemStat()) != null;
    }

    /**
     * To read from NBT data. This undoes {@link #toJson()} basically.
     * <p></p>
     *
     * @param iSource The MMOItem you are trying to read the NBT of
     */
    @Nullable
    public static StatHistory fromJson(@NotNull MMOItem iSource, @NotNull JsonObject json) {

        // Get the stat we're searching for
        JsonElement statEncode;
        JsonElement ogStatsEncode = null;
        JsonElement gemsEncode = null;
        JsonElement extEncode = null;
        JsonElement modEncode = null;

        // It has stat information right?
        if (json.has(ENC_STAT)) {
            statEncode = json.get(ENC_STAT);
        } else {
            return null;
        }
        if (json.has(ENC_OGS)) {
            ogStatsEncode = json.get(ENC_OGS);
        }
        if (json.has(ENC_GSS)) {
            gemsEncode = json.get(ENC_GSS);
        }
        if (json.has(ENC_EXS)) {
            extEncode = json.get(ENC_EXS);
        }
        if (json.has(ENC_MOD)) {
            modEncode = json.get(ENC_MOD);
        }

        // It is a primitive right
        if (!statEncode.isJsonPrimitive()) {
            return null;
        }
        if (ogStatsEncode != null && !ogStatsEncode.isJsonArray()) {
            return null;
        }
        if (gemsEncode != null && !gemsEncode.isJsonArray()) {
            return null;
        }
        if (extEncode != null && !extEncode.isJsonArray()) {
            return null;
        }
        if (modEncode != null && !modEncode.isJsonArray()) {
            return null;
        }

        // Get string
        String statInternalName = statEncode.getAsJsonPrimitive().getAsString();

        // Get stat
        ItemStat stat = MMOItems.plugin.getStats().get(statInternalName);

        // Nope
        if (stat == null) {
            return null;
        }

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
        if (sData == null) {
            return null;
        }

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
    @Nullable
    public static StatHistory fromNBTString(@NotNull MMOItem iSource, @NotNull String codedJson) {

        try {
            JsonParser pJSON = new JsonParser();
            JsonObject oJSON = pJSON.parse(codedJson).getAsJsonObject();
            return fromJson(iSource, oJSON);

        } catch (Throwable e) {
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
            ffp.activatePrefix(true, "统计历史");
            ffp.log(FriendlyFeedbackCategory.ERROR, "无法获取统计历史记录：$f{0}$b at $f{1}", e.getMessage(), e.getStackTrace()[0].toString());
            ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
            return null;
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NotNull
    public StatHistory clone() {
        StatHistory res = new StatHistory(getMMOItem(), getItemStat(), ((Mergeable<?>) getOriginalData()).clone());

        perGemstoneData.forEach((uuid, statData) -> res.perGemstoneData.put(uuid, ((Mergeable<?>) statData).clone()));
        perExternalData.forEach(statData -> res.perExternalData.add(((Mergeable<?>) statData).clone()));
        perModifierBonus.forEach((uuid, statData) -> res.perModifierBonus.put(uuid, ((Mergeable<?>) statData).clone()));

        return res;
    }

    private static final String ENC_STAT = "Stat", ENC_OGS = "OGStory", ENC_GSS = "Gemstory", ENC_EXS = "Exstory", ENC_MOD = "Mod";

    //region Methods not used
    public void assimilate(@NotNull StatHistory other) {
        if (other.getItemStat().getNBTPath().equals(getItemStat().getNBTPath())) {
            for (UUID exUID : other.getAllGemstones()) registerGemstoneData(exUID, other.getGemstoneData(exUID));
            for (StatData ex : other.getExternalData()) registerExternalData((ex));
            for (UUID exUID : other.getAllModifiers()) registerModifierBonus(exUID, other.getModifiersBonus(exUID));
        }
    }

    /**
     * @deprecated use {@link #clone()} followed by {@link #setParent(MMOItem)}
     */
    @Deprecated
    public StatHistory clone(@NotNull MMOItem newParent) {
        final StatHistory his = clone();
        his.setParent(newParent);
        return his;
    }

    /**
     * @deprecated See {@link MMOItem#computeStatHistory(ItemStat)} (ItemStat)}
     */
    @NotNull
    @Deprecated
    public static StatHistory from(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat) {
        return ofItem.computeStatHistory(ofStat);
    }

    @NotNull
    @Deprecated
    public static StatHistory from(@NotNull MMOItem parentItem, @NotNull ItemStat stat, boolean forceNew) {

        if (forceNew) {
            StatHistory newHist = new StatHistory(parentItem, stat, Objects.requireNonNull(parentItem.getData(stat)));
            parentItem.setStatHistory(stat, newHist);
            return newHist;
        }

        return parentItem.computeStatHistory((ItemStat<?, ?>) stat);
    }

    @NotNull
    @Deprecated
    public StatData recalculateUnupgraded(boolean withPurge) {
        return recalculate(withPurge, null);
    }

    /**
     * @see #isEmpty()
     */
    @Deprecated
    public boolean isClear() {
        return isEmpty();
    }

    /**
     * @see #fuseExternalData()
     */
    @Deprecated
    public void consolidateEXSH() {
        fuseExternalData();
    }
    //endregion
}
