package net.Indyuce.mmoitems.api.item.mmoitem;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MMOItem implements ItemReference {
	private final Type type;
	private final String id;

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

	@Override public Type getType() { return type; }

	@Override public String getId() { return id; }

	//region Item Stats - Where the Magic Happens
	/*
	 * Where data about all the item stats is stored. When the item is
	 * generated, this map is read and all the stats are applied. The order in
	 * which stats are added is not very important anymore
	 */
	@NotNull private final Map<ItemStat, StatData> stats = new HashMap<>();

	/**
	 * Will merge that data into this item's:
	 * <p></p>
	 * If the item does not have this stat yet, it will be set with <code>MMOItem.setData()</code>
	 * <p>If this data is not <code>Mergeable</code>, it will also be set</p>
	 * <p></p>
	 * Otherwise, the data will be merged with the previous. If an UUID is provided, it will also be
	 * stored as a GemStone in the history, allowing to be removed from the item with that same UUID.
	 */
	public void mergeData(@NotNull ItemStat stat, @NotNull StatData data, @Nullable UUID associatedGemStone) {
		//GEM//MMOItems.Log("Merging stone stat \u00a76" + stat.getNBTPath() + "\u00a77 into \u00a7c" + getType().getName() + " " + getId());

		// Do we already have the data?
		if (data instanceof Mergeable) {
			//GEM//MMOItems.Log("\u00a7a + \u00a77Mergeable");

			// Prepare to merge: Gather History (Also initializes the ORIGINAL stats)
			StatHistory<StatData> sHistory = StatHistory.From(this, stat);

			// As GemStone or as External?
			if (associatedGemStone != null) {
				//GEM//MMOItems.Log(" \u00a79++\u00a77 As Gemstone \u00a7b" + associatedGemStone.toString());

				// As GemStone
				sHistory.registerGemstoneData(associatedGemStone, data);

			// As External
			} else {
				//GEM//MMOItems.Log(" \u00a7c++\u00a77 As External");

				// As External, UUIDless modifier
				sHistory.registerExternalData(data);
			}

			// Recalculate
			setData(stat, sHistory.Recalculate());

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

	public boolean hasData(@NotNull ItemStat stat) {
		return (stats.get(stat) != null);
	}

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
			StatHistory<StatData> hist = getStatHistory(sat);
			if (hist != null) { clone.setStatHistory(sat, hist); }
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
	@NotNull public final Map<String, StatHistory<StatData>> mergeableStatHistory = new HashMap<>();

	/**
	 * Gets the history associated to this stat, if there is any
	 * <p></p>
	 * A stat history is basically the memmory of its original stats, from when it was created, its gem stone stats, those added by which gem, and its upgrade bonuses.
	 */
	@Nullable
	public  StatHistory<StatData> getStatHistory(@NotNull ItemStat stat) {
		try {

			// Well that REALLY should work
			return mergeableStatHistory.get(stat.getNBTPath());

		} catch (ClassCastException ignored) {
			return null;
		}
	}

	/**
	 * Sets the history associated to this stat.
	 * <p></p>
	 * A stat history is basically the memmory of its original stats, from when it was created, its gem stone stats, those added by which gem, and its upgrade bonuses.
	 */
	public void setStatHistory(@NotNull ItemStat stat, @NotNull StatHistory<StatData> hist) {
		mergeableStatHistory.put(stat.getNBTPath(), hist);
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

		// Does it have Upgrade Stat?
		if (hasData(ItemStats.UPGRADE)) {

			// Get that data
			UpgradeData data = (UpgradeData) getData(ItemStats.UPGRADE);

			// A template its all that's required
			return data.getTemplate() != null;
		}

		// Nope
		return false;
	}

	/**
	 * @return The upgrade level, or 0 if there is none.
	 */
	public int getUpgradeLevel() {

		// Does it have Upgrade Data?
		if (hasData(ItemStats.UPGRADE)) {

			// Return the registered level.
			return ((UpgradeData) getData(ItemStats.UPGRADE)).getLevel();
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
	@NotNull public UpgradeTemplate getUpgradeTemplate() {
		Validate.isTrue(hasUpgradeTemplate(), "This item has no Upgrade Information, do not call this method without checking first!");

		// All Right
		UpgradeData data = (UpgradeData) getData(ItemStats.UPGRADE);

		// That's the template
		return data.getTemplate();
	}

	/**
	 * Get the list of GemStones inserted into this item
	 */
	@NotNull public Set<GemstoneData> getGemStones() {

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
}
