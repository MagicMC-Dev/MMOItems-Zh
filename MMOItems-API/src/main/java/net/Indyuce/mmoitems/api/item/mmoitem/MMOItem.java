package net.Indyuce.mmoitems.api.item.mmoitem;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.util.Pair;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class MMOItem implements ItemReference {
	private final Type type;
	private final String id;

	/**
	 * Where data about all the item stats is stored. When the item is
	 * generated, this map is read and all the stats are applied. The order in
	 * which stats are added is not very important anymore
	 */
	@NotNull
	private final Map<ItemStat, StatData> stats = new HashMap<>();

	/**
	 * Constructor used to generate an ItemStack based on some stat data
	 *
	 * @param type
	 *            The type of the item you want to create
	 * @param id
	 *            The id of the item, make sure it is different from other
	 *            existing items not to interfere with MI features like the
	 *            dynamic item updater
	 */
	public MMOItem(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public Type getType() { return type; }

	@Override
	public String getId() { return id; }

	/**
	 * Will merge that data into this item:
	 * <p></p>
	 * If the item does not have this stat yet, it will be set with <code>MMOItem.setData()</code>
	 * <p>If this data is not <code>Mergeable</code>, it will also be set</p>
	 * <p></p>
	 * Otherwise, the data will be merged with the previous. If an UUID is provided, it will also be
	 * stored as a GemStone in the history, allowing to be removed from the item with that same UUID.
	 */
	public void mergeData(@NotNull ItemStat stat, @NotNull StatData data, @Nullable UUID associatedGemStone) {
		//GEM//MMOItems.log("Merging stone stat \u00a76" + stat.getNBTPath() + "\u00a77 into \u00a7c" + getType().getName() + " " + getId());

		// Do we already have the data?
		if (data instanceof Mergeable) {
			//GEM//MMOItems.log("\u00a7a + \u00a77Mergeable");

			// Prepare to merge: Gather History (Also initializes the ORIGINAL stats)
			StatHistory sHistory = StatHistory.from(this, stat);

			// As GemStone or as External?
			if (associatedGemStone != null) {
				//GEM//MMOItems.log(" \u00a79++\u00a77 As Gemstone \u00a7b" + associatedGemStone.toString());

				// As GemStone
				sHistory.registerGemstoneData(associatedGemStone, data);

			// As External
			} else {
				//GEM//MMOItems.log(" \u00a7c++\u00a77 As External");

				// As External, UUIDless modifier
				sHistory.registerExternalData(data);
			}

			// Recalculate
			//GEM//MMOItems.log(" \u00a76+++\u00a77 Recalculating...");
			//HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Gem Application Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
			setData(stat, sHistory.recalculate(getUpgradeLevel()));

	 	// Merging means replacing if it cannot be merged
		} else {

			// Override Completely
			setData(stat, data);
		}
	}

	public void setData(@NotNull ItemStat stat, @NotNull StatData data) {
		stats.put(stat, data);
	}

	public void replaceData(@NotNull ItemStat stat, @NotNull StatData data) {
		stats.replace(stat, data);
	}

	public void removeData(@NotNull ItemStat stat) {
		stats.remove(stat);
	}

	public StatData getData(@NotNull ItemStat stat) {
		return stats.get(stat);
	}

	public boolean hasData(@NotNull ItemStat stat) { return (stats.get(stat) != null); }

	/**
	 * @return Collection of all item stats which have some data on this mmoitem
	 */
	@NotNull public Set<ItemStat> getStats() {
		return stats.keySet();
	}
	//endregion

	//region Building and such
	/**
	 * @return A class which lets you build this mmoitem into an ItemStack
	 */
	@NotNull public ItemStackBuilder newBuilder() {
		return new ItemStackBuilder(this);
	}

	/***
	 * @return A cloned instance of this mmoitem. This does NOT clone the
	 *         StatData instances! If you edit these statDatas, the previous
	 *         mmoitem will be edited as well.
	 */
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public MMOItem clone() {
		MMOItem clone = new MMOItem(type, id);

		// Clone them stats and histories
		for (ItemStat sat : stats.keySet()) {

			// Copy Stat Datas themselves
			clone.stats.put(sat, stats.get(sat));

			// Copy Histories
			StatHistory hist = getStatHistory(sat);
			if (hist != null) { clone.setStatHistory(sat, hist.clone(clone)); }
		}

		// Thats it
		return clone;
	}
	//endregion

	//region Stat History Stuff
	/*
	 * When merging stats (like applying a gemstone), the item must remember which were
	 * its original stats, and from which gem stone came each stat, in order to allow
	 * removal of gem stones in the future. This is where that is remembered.
	 */
	@NotNull
	private final Map<ItemStat, StatHistory> mergeableStatHistory = new HashMap<>();

	/**
	 * Gets the history associated to this stat, if there is any
	 * <p></p>
	 * A stat history is basically the memmory of its original stats, from when it was created, its gem stone stats, those added by which gem, and its upgrade bonuses.
	 */
	@Nullable
	public StatHistory getStatHistory(@NotNull ItemStat stat) {

		/*
		 * As an assumption for several enchantment recognition operations,
		 * Enchantment data must never be clear and lack history. This is
		 * the basis for when an item is 'old'
		 */
		if (stat instanceof Enchants) { return mergeableStatHistory.getOrDefault(stat, StatHistory.from(this, stat, true)); }

		/*
		 * Normal stat, just fetch.
		 */
		try {

			// Well that REALLY should work
			return mergeableStatHistory.get(stat);

		} catch (ClassCastException ignored) {
			return null;
		}
	}

	@NotNull
	public ArrayList<StatHistory> getStatHistories() {
		return new ArrayList<>(mergeableStatHistory.values());
	}

	/**
	 * Sets the history associated to this stat.
	 * <p></p>
	 * A stat history is basically the memmory of its original stats, from when it was created, its gem stone stats, those added by which gem, and its upgrade bonuses.
	 */
	public void setStatHistory(@NotNull ItemStat stat, @NotNull StatHistory hist) {
		mergeableStatHistory.put(stat, hist);
	}

	//region Other API

	/**
	 * @return The tier of this item, if it has one.
	 */
	@Nullable
	public ItemTier getTier() {
		return hasData(ItemStats.TIER) ? MMOItems.plugin.getTiers().get(stats.get(ItemStats.TIER).toString()) : null;
	}

	/**
	 * A MMOItem from the template only has damage
	 * from the ITEM_DAMAGE stat
	 *
	 * @return The damage suffered by this item
	 */
	@Deprecated
	public int getDamage() {

		// Does it use MMO Durability?
		int maxDurability = hasData(ItemStats.MAX_DURABILITY) ? SilentNumbers.round(((DoubleData) getData(ItemStats.MAX_DURABILITY)).getValue()) : -1;

		if (maxDurability > 0) {

			// Apparently we must do this
			NBTItem nbtItem = newBuilder().buildNBT();

			// Durability
			int durability = hasData(ItemStats.CUSTOM_DURABILITY) ? SilentNumbers.round(((DoubleData) getData(ItemStats.CUSTOM_DURABILITY)).getValue()) : maxDurability;

			// Damage is the difference between max and current durability
			return maxDurability - durability;

		} else {

			// Use vanilla durability
			return hasData(ItemStats.ITEM_DAMAGE) ? SilentNumbers.round(((DoubleData) getData(ItemStats.ITEM_DAMAGE)).getValue()) : 0;
		}
	}

	/**
	 * A MMOItem from the template only has damage
	 * from the ITEM_DAMAGE stat
	 *
	 * @param damage The damage suffered by this item
	 */
	@Deprecated
	public void setDamage(int damage) {

		// Too powerful
		if (hasData(ItemStats.UNBREAKABLE)) { return; }

		// Does it use MMO Durability?
		int maxDurability = hasData(ItemStats.MAX_DURABILITY) ? SilentNumbers.round(((DoubleData) getData(ItemStats.MAX_DURABILITY)).getValue()) : -1;

		if (maxDurability > 0) {

			// Apparently we must do this
			NBTItem nbtItem = newBuilder().buildNBT();

			// Durability
			setData(ItemStats.CUSTOM_DURABILITY, new DoubleData(maxDurability - damage));

			// Scale damage
			Material mat = hasData(ItemStats.MATERIAL) ? ((MaterialData) getData(ItemStats.MATERIAL)).getMaterial() : Material.GOLD_INGOT;
			double multiplier = ((double) damage) * ((double) mat.getMaxDurability()) / ((double) maxDurability);
			if (multiplier == 0) { return; }

			// Set to some decent amount of damage
			setData(ItemStats.ITEM_DAMAGE, new DoubleData(multiplier));

		} else {

			// Use vanilla durability
			setData(ItemStats.ITEM_DAMAGE, new DoubleData(damage));
		}
	}
	//endregion

	//region Upgrading API

	/**
	 * Upgrades this MMOItem one level.
	 * <p></p>
	 * <b>Make sure to check {@link #hasUpgradeTemplate()} before calling.</b>
	 */
	public void upgrade() {

		// Upgrade through the template's API
		getUpgradeTemplate().upgrade(this);
	}

	/**
	 * Whether or not this item has all the information
	 * required to call
	 */
	public boolean hasUpgradeTemplate() {
		return hasData(ItemStats.UPGRADE) && ((UpgradeData) getData(ItemStats.UPGRADE)).getTemplate() != null;
	}

	/**
	 * @return The upgrade level, or 0 if there is none.
	 */
	public int getUpgradeLevel() {
		return hasData(ItemStats.UPGRADE) ? ((UpgradeData) getData(ItemStats.UPGRADE)).getLevel() : 0;
	}

	/**
	 * @return The upgrade level, or 0 if there is none.
	 */
	public int getMaxUpgradeLevel() {

		// Does it have Upgrade Data?
		if (hasData(ItemStats.UPGRADE)) {

			// Return the registered level.
			return ((UpgradeData) getData(ItemStats.UPGRADE)).getMax();
		}

		// Nope? Well its level 0 I guess.
		return 0;
	}

	/**
	 * <b>Make sure to check {@link #hasUpgradeTemplate()} before calling.</b>
	 * <p></p>
	 * This will fail and throw an exception if the MMOItem has no upgrade template.
	 * @return The upgrade template by which the MMOItem would upgrade normally.
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull public UpgradeTemplate getUpgradeTemplate() {
		Validate.isTrue(hasUpgradeTemplate(), "Item has no upgrade information");

		// All Right
		UpgradeData data = (UpgradeData) getData(ItemStats.UPGRADE);

		// That's the template
		return data.getTemplate();
	}

	/**
	 * Get the list of GemStones inserted into this item
	 */
	@NotNull
	public Set<GemstoneData> getGemStones() {

		// Got gem sockets?
		if (hasData(ItemStats.GEM_SOCKETS)) {

			// Get Data
			GemSocketsData data = (GemSocketsData) getData(ItemStats.GEM_SOCKETS);

			// Thats it
			return data.getGemstones();

		// Has no gem sockets
		} else {

			// Empty Set
			return new HashSet<>();
		}
	}
	//endregion

	//region Gem Sockets API

	/**
	 * It is not 100% fool proof, since some GemStones just don't have
	 * enough information to be extracted (legacy gemstones).
	 * <p></p>
	 * Note that this is somewhat an expensive operation, restrain
	 * from calling it much because it completely loads all the stats
	 * of every Gem Stone.
	 *
	 * @return The list of GemStones contained here.
	 */
	@NotNull
	public List<Pair<GemstoneData, MMOItem>> extractGemstones() {

		// Found?
		final @Nullable GemSocketsData thisSocketsData = (GemSocketsData) getData(ItemStats.GEM_SOCKETS);
		if (thisSocketsData == null)
			return new ArrayList<>();

		// Find restored items
		final List<Pair<GemstoneData, MMOItem>> pairs = new ArrayList<>();
		for (GemstoneData gem : thisSocketsData.getGemstones()) {
			final MMOItem restored = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(gem.getMMOItemType()), gem.getMMOItemID());
			if (restored != null)
				pairs.add(Pair.of(gem, restored));
		}

		// If RevID updating, no need to identify stats
		if (MMOItemReforger.gemstonesRevIDWhenUnsocket)
			return pairs;

		// Identify actual stats
		for (StatHistory hist : mergeableStatHistory.values())
			for (Pair<GemstoneData, MMOItem> gem : pairs) {
				final StatData historicGemData = hist.getGemstoneData(gem.getKey().getHistoricUUID());
				if (historicGemData != null)
					gem.getValue().setData(hist.getItemStat(), historicGemData);
			}

		return pairs;
	}

	/**
	 * Extracts a single gemstone. Note that this only builds the original Gemstone MMOItem, and if you
	 * wish to actually remove the GemStone, you must do so through {@link #removeGemStone(UUID, String)}
	 *
	 * @param gem Gemstone that you believe is in here
	 * @return The gemstone as it was when inserted, or <code>null</code>
	 *         if such gemstone is not in here.
	 * @see #extractGemstones() More optimized method for extracting all gemstones at the same time.
	 */
	@Nullable
	public MMOItem extractGemstone(@NotNull GemstoneData gem) {

		final MMOItem restored = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(gem.getMMOItemType()), gem.getMMOItemID());
		if (restored == null)
			return null;

		// If RevID updating, no need to identify stats
		if (MMOItemReforger.gemstonesRevIDWhenUnsocket)
			return restored;

		// Identify actual stats
		for (StatHistory hist : mergeableStatHistory.values()) {
			final StatData historicGemData = hist.getGemstoneData(gem.getHistoricUUID());
			if (historicGemData != null)
				restored.setData(hist.getItemStat(), historicGemData);
		}

		return restored;
	}

	/**
	 * Deletes this UUID from all stat histories.
	 *
	 * @param gemUUID UUID of gem to remove
	 * @param color Color of the gem socket to restore. {@code null} to not restore socket.
	 */
	@SuppressWarnings("ConstantConditions")
	public void removeGemStone(@NotNull UUID gemUUID, @Nullable String color) {

		// Get gemstone data
		if (!hasData(ItemStats.GEM_SOCKETS))
			return;

		//GEM//MMOItems.log("\u00a7b-\u00a78-\u00a79-\u00a77 Extracting \u00a7e" + gemUUID.toString());
		StatHistory gemStory = StatHistory.from(this, ItemStats.GEM_SOCKETS);

		/*
		 * We must only find the StatData where this gem resides,
		 * and eventually the StatHistories of the affected stats
		 * will purge themselves from extraneous gems (that are
		 * no longer registered onto the GEM_SOCKETS history).
		 */
		if (((GemSocketsData) gemStory.getOriginalData()).removeGem(gemUUID, color))
			return;

		// Attempt gems
		for (UUID gemDataUUID : gemStory.getAllGemstones())
			if (((GemSocketsData) gemStory.getGemstoneData(gemDataUUID)).removeGem(gemUUID, color))
				return;

		// Attempt externals
		for (StatData externalData : gemStory.getExternalData())
			if (((GemSocketsData) externalData).removeGem(gemUUID, color))
				return;

		// Attempt gems
		for (UUID gemDataUUID : gemStory.getAllModifiers())
			if (((GemSocketsData) gemStory.getModifiersBonus(gemDataUUID)).removeGem(gemUUID, color))
				return;
	}
	//endregion
}
