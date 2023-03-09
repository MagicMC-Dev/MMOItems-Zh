package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.stat.GemUpgradeScaling;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class GemstoneData {
    private final Set<AbilityData> abilities = new HashSet<>();
    private final List<PotionEffectData> effects = new ArrayList<>();
    private final Map<ItemStat, Double> stats = new HashMap<>();
    private final String name;
    @NotNull
    private final UUID historicUUID;
    @Nullable
    private final String mmoitemType;
    @Nullable
    private final String mmoitemID;

    @Nullable
    private String socketColor;
    @Nullable
    private Integer levelPut;

    public GemstoneData cloneGem() {

        GemstoneData ret = new GemstoneData(getName(), getMMOItemType(), getMMOItemID(), getSocketColor(), getHistoricUUID());
        for (AbilityData d : abilities)
            ret.addAbility(d);
        for (PotionEffectData d : effects)
            ret.addPermanentEffect(d);
        for (ItemStat d : stats.keySet())
            ret.setStat(d, stats.get(d));
        ret.setLevel(getLevel());

        return ret;
    }

    /**
     * Gemstone equals method is for practical purposes and only checks that
     * this other thing is both a GemstoneData and has the same UUID.
     *
     * @param obj Object to compare with
     * @return <code>true</code> if they have the same {@link #getHistoricUUID()}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GemstoneData)) {
            return false;
        }

        return ((GemstoneData) obj).getHistoricUUID().equals(getHistoricUUID());
    }

    @Nullable
    public String getMMOItemType() {
        return mmoitemType;
    }

    @Nullable
    public String getMMOItemID() {
        return mmoitemID;
    }

    /**
     * If known, the socket colour this gem was put into
     */
    @Nullable
    public String getSocketColor() {
        return socketColor;
    }

    /**
     * This constructor is not really performance friendly. It should only be
     * used when applying gem stones to keep max performance.
     */
    public GemstoneData(@NotNull JsonObject object) {

        // GEt Name
        name = object.get("Name").getAsString();

        // Get Stats
        //object.getAsJsonObject("Stats").entrySet() .forEach(entry -> this.stats.put(MMOItems.plugin.getStats().get(entry.getKey()), entry.getValue().getAsDouble()));

        // Get Abilities
        //object.getAsJsonArray("Abilities").forEach(element -> this.abilities.add(new AbilityData(element.getAsJsonObject())));

        // Get Permanent Potion Effects
        //object.getAsJsonObject("Effects").entrySet().forEach(entry -> this.effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));

        // Get assigned HUUID, Assign new if its an olden item without it :>
        JsonElement uuid = object.get("History");
        if (uuid != null) {

            // Its of this gen of gemstones...
            String hUUID = uuid.getAsString();
            UUID hisUUID = MMOUtils.UUIDFromString(hUUID);
            if (hisUUID != null) {
                historicUUID = hisUUID;
            } else {
                historicUUID = UUID.randomUUID();
            }

            // Get Type and IDs
            JsonElement gType = object.get("Type");
            JsonElement gID = object.get("Id");
            if (gType != null) {
                mmoitemType = gType.getAsString();
            } else {
                mmoitemType = null;
            }
            if (gID != null) {
                mmoitemID = gID.getAsString();
            } else {
                mmoitemID = null;
            }

            JsonElement level = object.get("Level");
            if (level != null && level.isJsonPrimitive()) {
                levelPut = level.getAsJsonPrimitive().getAsInt();
            } else {
                levelPut = null;
            }
            //LVL//MMOItems.log("\u00a73 -\u00a7b-\u00a73-\u00a77 Read Level: \u00a7b" + levelPut);

            JsonElement color = object.get("Color");
            if (color != null && color.isJsonPrimitive()) {
                socketColor = color.getAsJsonPrimitive().getAsString();
            } else {
                socketColor = null;
            }

        } else {
            historicUUID = UUID.randomUUID();
            mmoitemID = null;
            mmoitemType = null;
            socketColor = null;
        }
    }

    /**
     * Create a GemStoneData from a GemStone MMOItem.
     * <p></p>
     * Basically extracts all the useable stats from the MMOItem, to have them ready to apply onto another MMOItem.
     *
     * @param color Color of the slot this gem was inserted onto.
     */
    public GemstoneData(@NotNull LiveMMOItem gemStoneMMOItem, @Nullable String color) { this(gemStoneMMOItem, color, UUID.randomUUID()); }

    /**
     * Create a GemStoneData from a GemStone MMOItem.
     * <p></p>
     * Basically extracts all the useable stats from the MMOItem, to have them ready to apply onto another MMOItem.
     *
     * @param color Color of the slot this gem was inserted onto.
     */
    public GemstoneData(@NotNull LiveMMOItem gemStoneMMOItem, @Nullable String color, @NotNull UUID forcedHistoryUUID) {

        // Get Name to Display
        name = MMOUtils.getDisplayName(gemStoneMMOItem.getNBT().getItem());

        // Extract abilities from the Gem Stone MMOItem into a more accessible form
        if (gemStoneMMOItem.hasData(ItemStats.ABILITIES)) {
            abilities.addAll(((AbilityListData) gemStoneMMOItem.getData(ItemStats.ABILITIES)).getAbilities());
        }

        // Extract permenent effects from the Gem Stone MMOItem into a more accessible form
        if (gemStoneMMOItem.hasData(ItemStats.PERM_EFFECTS)) {
            effects.addAll(((PotionEffectListData) gemStoneMMOItem.getData(ItemStats.PERM_EFFECTS)).getEffects());
        }

        // Generate own historic UUID
        historicUUID = forcedHistoryUUID;
        mmoitemID = gemStoneMMOItem.getId();
        mmoitemType = gemStoneMMOItem.getType().getId();
        socketColor = color;
    }

    /**
     * This is a completely empty builder.
     * <p></p>
     * You may add whatever you want with <code>addAbility()</code>,<code>addPermamentEffect</code>, or most widely usedly, <code>setStat()</code>.
     * <p></p>
     *
     * @param name Name to display in the lore of the item when you put the gemstone into it.
     * @deprecated This gem stone will not have a type/id and will cause problems when trying to remove it from items with a consumable.
     */
    public GemstoneData(@NotNull String name) {
        this.name = name;
        mmoitemID = null;
        mmoitemType = null;
        socketColor = null;
        historicUUID = UUID.randomUUID();
    }

    /**
     * This is at which level (of the item) the gemstone was placed onto the item.
     * <p>A null level means this gem does not scale.</p>
     * <p></p>
     * For scaling purposes of stat {@link GemUpgradeScaling}
     */
    public void setLevel(@Nullable Integer l) {
        //LVL//MMOItems.log("\u00a73 -\u00a7b-\u00a73-\u00a77 Set Level: \u00a7b" + l);
        levelPut = l;
    }

    /**
     * This is at which level (of the item) the gemstone was placed onto the item.
     * <p>A null level means this gem does not scale.</p>
     * <p></p>
     * For scaling purposes of stat {@link GemUpgradeScaling}
     */
    @Nullable
    public Integer getLevel() {
        return levelPut;
    }

    /**
     * Does this gem scale with item upgrades?
     */
    public boolean isScaling() {
        return levelPut != null;
    }

    /**
     * This is a completely empty builder.
     * <p></p>
     * You may add whatever you want with <code>addAbility()</code>,<code>addPermamentEffect</code>, or most widely usedly, <code>setStat()</code>.
     *
     * @param name  Name to display in the lore of the item when you put the gemstone into it.
     * @param color Color of the socket this gem is inserted onto
     */
    public GemstoneData(@NotNull String name, @Nullable String type, @Nullable String id, @Nullable String color) {
        this.name = name;
        mmoitemID = id;
        mmoitemType = type;
        socketColor = color;
        historicUUID = UUID.randomUUID();
    }

    /**
     * This is a completely empty builder.
     * <p></p>
     * You may add whatever you want with <code>addAbility()</code>,<code>addPermamentEffect</code>, or most widely usedly, <code>setStat()</code>.
     *
     * @param name  Name to display in the lore of the item when you put the gemstone into it.
     * @param color Color of the socket this gem is inserted onto
     */
    public GemstoneData(@NotNull String name, @Nullable String type, @Nullable String id, @Nullable String color, @NotNull UUID uiid) {
        this.name = name;
        mmoitemID = id;
        mmoitemType = type;
        socketColor = color;
        historicUUID = uiid;
    }

    /**
     * Add an ability to this Gem Stone
     */
    public void addAbility(@NotNull AbilityData ability) {
        abilities.add(ability);
    }

    /**
     * Add a permanent potion effect to this Gem Stone
     */
    public void addPermanentEffect(@NotNull PotionEffectData effect) {
        effects.add(effect);
    }

    /**
     * Add an ItemStat to this gemstone
     */
    public void setStat(@NotNull ItemStat stat, double value) {
        stats.put(stat, value);
    }

    /**
     * Get the display text for when this is put into lore.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * If known, the socket colour this gem was put into
     */
    public void setColour(@Nullable String color) {
        socketColor = color;
    }

    /**
     * Want to know which stats were given to the item by this gemstone (after applying upgrade scaling and such)? Use this!
     */
    @NotNull
    public UUID getHistoricUUID() {
        return historicUUID;
    }

    /**
     * To store onto the NBT of the item.
     */
    @NotNull
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("Name", name);
        object.addProperty("History", historicUUID.toString());
        if (mmoitemID != null) {
            object.addProperty("Id", mmoitemID);
        }
        if (mmoitemType != null) {
            object.addProperty("Type", mmoitemType);
        }
        if (levelPut != null) {
            //LVL//MMOItems.log("\u00a73 -\u00a7b-\u00a73-\u00a77 Saving Level: \u00a7b" + levelPut);
            object.addProperty("Level", levelPut);
        }
        object.addProperty("Color", socketColor);

		/*
		 * These seem obsolete. Abilities, effects, and stats, are merged into the
		 * main item anyway so it seems redundant to also save them in the gem stone?
		 *
		 * Plus, the stats get cleared when the gemstone is applied, so ???
		 *
		JsonObject stats = new JsonObject();
		for (ItemStat stat : this.stats.keySet())
			stats.addProperty(stat.getId(), this.stats.get(stat));
		object.add("Stats", stats);

		JsonArray abilities = new JsonArray();
		this.abilities.forEach(ability -> abilities.add(ability.toJson()));
		object.add("Abilities", abilities);

		JsonObject effects = new JsonObject();
		this.effects.forEach(effect -> effects.addProperty(effect.getType().getName(), effect.getLevel()));
		object.add("Effects", effects);
		*/

        return object;
    }
}