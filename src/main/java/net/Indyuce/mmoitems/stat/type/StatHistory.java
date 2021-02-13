package net.Indyuce.mmoitems.stat.type;

import com.google.gson.*;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
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
public class StatHistory<S extends StatData> {

    /*
     * Which stat is this the history of?
     */
    @NotNull ItemStat itemStat;

    /**
     * Which stat is this the history of?
     */
    @NotNull public ItemStat getItemStat() { return itemStat; }

    /*
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull MMOItem parent;

    /**
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull public MMOItem getMMOItem() { return parent; }

    /*
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull S originalData;

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull public S getOriginalData() { return originalData; }

    /*
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @NotNull HashMap<UUID, S> perGemstoneData = new HashMap<>();

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @NotNull public S getGemstoneData(UUID of) { return perGemstoneData.get(of); }

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     * <p></p>
     * Basically, supposing this stands for a double data like <i>Attack Damage</i>:
     * <p>originally <code>+5</code>, now at level 2, with <code>+0.25</code> per level</p>
     * The value of this stat data will be <b><code>+5.5</code></b>
     */
    public void registerGemstoneData(@NotNull UUID of, @NotNull S data) { perGemstoneData.put(of, data); }

    /*
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     */
    @NotNull ArrayList<S> perExternalData = new ArrayList<>();

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
    @NotNull public ArrayList<S> getExternalData() { return perExternalData; }

    /**
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     * <p></p>
     * <i>External Data</i> is just a fancy name for '<i>GemStones with no UUID</i>'
     * <p>They act as gem stones, adding together to produce the total of the item, but cannot be removed, since there is no way to tell them from each other.</p>
     * Well, I guess whatever plugin is putting them here may remove them by editing the list directly with <code>StatHistory.getExternalData()</code>
     */
    public void registerExternalData(@NotNull S data) { perExternalData.add(data); }

    /**
     * Gets the stat history of this item. <b>The stat must be <code>Mergeable</code></b>
     * <p></p>
     * If the item has no stat history, it will be created anew and appended; the current stat values will become the 'Original' ones,
     * and will be forever unchangeable.
     * <p></p>
     * <b>Make sure the item has the stat present</b>
     */
    @NotNull public static StatHistory<StatData> From(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat) {

        // Get history :B
        StatHistory<StatData> hist = ofItem.getStatHistory(ofStat);

        // Found? Thats it
        if (hist != null) {
            //GEM//MMOItems.Log("Found Stat History of \u00a76" + ofStat.getNBTPath() + "\u00a77 in this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());
            return hist; }
        //GEM//MMOItems.Log("\u00a7aCreated Hisotry of \u00a76" + ofStat.getNBTPath() + "\u00a7a of this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());

        // That is Mergeable right...
        Validate.isTrue(ofStat.getClearStatData() instanceof Mergeable, "Non-Mergeable stat data wont have a Stat History; they cannot be modified dynamically in the first place.");

        // Get original data
        StatData original = ofItem.getData(ofStat);
        if (original == null) {
            original = ofStat.getClearStatData();
            //GEM// MMOItems.Log("\u00a7e   +\u00a77 Item didnt have this stat, original set as blanc.");
        }
        else {
            original = ((Mergeable) original).cloneData();
            //GEM//MMOItems.Log("\u00a7a   +\u00a77 Found original data");
        }

        // Create new
        hist = new StatHistory<>(ofItem, ofStat, original);

        // Append to the item
        ofItem.setStatHistory(ofStat, hist);

        // Thats it
        return hist;
    }

    /**
     * Simplemost constructor, shall never be actually called outside this class.
     * <p></p>
     * Use <code>StatHistory.From()</code> to get the stat history associated to an item.
     */
    StatHistory(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat, @NotNull S ogData) { itemStat = ofStat; originalData = ogData; parent = ofItem; }

    /**
     * This recalculates final value of the stats of the item.
     */
    @NotNull public S Recalculate() {

        // Double Data shall account for upgrade levels.
        if (originalData instanceof DoubleData) { return Recalculate_AsDoubleData(); }

        return Recalculate_ThroughClone();
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
    S Recalculate_AsDoubleData() {

        // Start out with original value?
        double recalculated = ((DoubleData) getOriginalData()).getValue();

        // Process Upgrades
        UpgradeData data = (UpgradeData) getMMOItem().getData(ItemStats.UPGRADE);
        if (data != null) {

            // Apply scaling
            UpgradeTemplate template = data.getTemplate();
        }

        // Alr time to merge all
        DoubleData ret = new DoubleData(recalculated);

        // Add up gemstones
        for (S d : perGemstoneData.values()) { ret.merge(d); }

        // Add up externals
        for (S d : perExternalData) { ret.merge(d); }

        // Return result
        return (S) ret;
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
    S Recalculate_ThroughClone() {

        // Just clone bro
        S ret = (S) ((Mergeable) getOriginalData()).cloneData();

        // Add up gemstones
        for (S d : perGemstoneData.values()) { ((Mergeable) ret).merge(d); }

        // Add up externals
        for (S d : perExternalData) { ((Mergeable) ret).merge(d); }

        // Return result
        return (S) ret;
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

        // Original data
        object.add(enc_OGS, ItemTag.compressTags(getItemStat().getAppliedNBT(getOriginalData())));

        // Kompress Arrays
        JsonArray gemz = new JsonArray();

        // Compress I suppose
        for (UUID gem : perGemstoneData.keySet()) {

            // As Json Object
            JsonObject yes = new JsonObject();

            // Compress tags
            JsonArray yesCompressed = ItemTag.compressTags(getItemStat().getAppliedNBT(getGemstoneData(gem)));

            // Put
            yes.add(gem.toString(), yesCompressed);

            // Actually Include
            gemz.add(yes);
        }

        // Include
        object.add(enc_GSS, gemz);

        // Kompress Arrays
        JsonArray externals = new JsonArray();

        // Compress I suppose
        for (StatData ex : perExternalData) {

            // Put
            externals.add(ItemTag.compressTags(getItemStat().getAppliedNBT(ex)));
        }

        // Include
        object.add(enc_EXS, externals);

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
    @Nullable public static StatHistory<StatData> fromJson(@NotNull MMOItem iSource, @NotNull JsonObject json) {

        // Get the stat we're searching for
        JsonElement statEncode;
        JsonElement ogStatsEncode;
        JsonElement gemsEncode = null;
        JsonElement extEncode = null;

        // It has stat information right?
        if (!json.has(enc_Stat)) { return null; } else { statEncode = json.get(enc_Stat); }
        if (!json.has(enc_OGS)) { return null; } else { ogStatsEncode = json.get(enc_OGS); }
        if (json.has(enc_GSS)) { gemsEncode = json.get(enc_GSS); }
        if (json.has(enc_EXS)) { extEncode = json.get(enc_EXS); }

        // It is a primitive right
        if (!statEncode.isJsonPrimitive()) { return null; }
        if (!ogStatsEncode.isJsonArray()) { return null; }
        if (gemsEncode != null && !gemsEncode.isJsonArray()) { return null; }
        if (extEncode != null && !extEncode.isJsonArray()) { return null; }

        // Get string
        String statInternalName = statEncode.getAsJsonPrimitive().getAsString();

        // Get stat
        ItemStat stat = MMOItems.plugin.getStats().get(statInternalName);

        // Nope
        if (stat == null) { return null; }

        // Decompress tags
        ArrayList<ItemTag> ogDecoded = ItemTag.decompressTags(ogStatsEncode.getAsJsonArray());

        // To know the stat it was
        StatData sData = stat.getLoadedNBT(ogDecoded);

        // Validate non null
        if (sData == null) { return null; }

        // Can now generate stat history
        StatHistory<StatData> sHistory = new StatHistory<>(iSource, stat, sData);

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
                        UUID actualUUID = GemstoneData.UUIDFromString(gemUUID);

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

        return sHistory;
    }

    /**
     * To read from NBT data. This reverses {@link #toNBTString()} basically.
     * <p></p>
     * Will be null if some error happens
     */
    @Nullable public static StatHistory<StatData> fromNBTString(@NotNull MMOItem iSource, @NotNull String codedJson) {

        // Attempt
        try {

            // Make JSON Parser
            JsonParser pJSON = new JsonParser();

            // Parse as array
            JsonObject oJSON = pJSON.parse(codedJson).getAsJsonObject();

            // Bake
            return fromJson(iSource, oJSON);

        } catch (Throwable e) {

            // Annoying
            MMOItems.SLog("Error produced when getting stat history: \u00a7c" + e.getMessage() + "\u00a77 at \u00a76 " + e.getStackTrace()[0]);

            return null;
        }
    }

    static final String enc_Stat = "Stat";
    static final String enc_OGS = "OGStory";
    static final String enc_GSS = "Gemstory";
    static final String enc_EXS = "Exstory";
}
