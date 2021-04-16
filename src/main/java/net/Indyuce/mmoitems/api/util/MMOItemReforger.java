package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.RevisionID;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class to manage modification of items with reference to what they used to be
 * (and apparently also used to automatically apply SoulBounds):
 *
 * <p><code><b>updating</b></code> refers to changing the base stats
 * of a MMOItem instance to what the template currently has, usually
 * keeping gem stones and upgrade level. This wont reroll RNG stats.</p>
 *
 * <p><code><b>reforging</b></code> same thing as updating, but rerolling
 * the RNG stats - basically transferring the data specified by the
 * {@link ReforgeOptions} into a new item of the same Type-ID</p>
 *
 * @author Gunging, ? (Indyuce is my guess)
 */
public class MMOItemReforger {
	
	//region Config Values
	static int autoSoulboundLevel = 1;
	static int defaultItemLevel = -32767;
	static boolean keepTiersWhenReroll = true;
	
	public static void reload() {
		autoSoulboundLevel = MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1);
		defaultItemLevel = MMOItems.plugin.getConfig().getInt("item-revision.default-item-level", -32767);
		keepTiersWhenReroll = MMOItems.plugin.getConfig().getBoolean("item-revision.keep-tiers");
	}
	//endregion

	// region Item Data

	// Raw NBT Item
	@NotNull private final NBTItem nbtItem;

	// ItemStack size
	private final int amount;

	// Not initialized at first for performance reasons
	private MMOItem mmoItem;

	// Data
	private final Map<ItemStat, StatHistory> itemDataHistory = new HashMap<>();

	//endregion

	//region Cached stuff

	// Stripped Name
	@Nullable String cachedName;

	// Grayish Lore
	@NotNull ArrayList<String> cachedLore = new ArrayList<>();

	// Extraneous Enchantments
	@Nullable EnchantListData cachedEnchantments;

	// Gem Stones
	@Nullable GemSocketsData cachedGemStones;

	// Upgrade Level
	@Nullable UpgradeData cachedUpgradeLevel;

	// Soulbound
	private StatData cachedSoulbound;

	//endregion

	/**
	 *  Prepare to reforge this MMOItem (starts out as NBTItem due to backend reasons).
	 *  @param nbt <b>Make sure {@link NBTItem#hasType()} returns true for this</b>.
	 */
	public MMOItemReforger(@NotNull NBTItem nbt) {
		this.nbtItem = nbt;
		this.amount = nbt.getItem().getAmount();
	}

	/**
	 * Apply a quick soulbound based on the config value <code>soulbound.auto-bind.level</code> (default = 1)
	 */
	public void applySoulbound(@NotNull Player p) {
		applySoulbound(p, autoSoulboundLevel);
	}

	/**
	 * Apply a quick soulbound of this level
	 */
	public void applySoulbound(@NotNull Player p, int level) {

		// Initialize as Live MMOItem
		loadLiveMMOItem();

		// Override Soulbound Data
		mmoItem.setData(ItemStats.SOULBOUND, new SoulboundData(p.getUniqueId(), p.getName(), level));
	}

	/**
	 *  This method updates the base stats of this item to what
	 *  the template currently has, only rerolling RNGs where the
	 *  probability of getting the same roll is less than 5%
	 *
	 * @param options Which data to 'keep'
	 * @param p There is an option where an item's base stats will be better
	 *          if the player who first generates the item is a higher level.
	 *          This player will be used to fulfill that operation.
	 *          <p></p>
	 *          If <code>null</code>, the default modifiers
	 *          specified in the config is used.
	 */
	public void update(@Nullable Player p, @NotNull ReforgeOptions options) {
		if (p == null) { update((RPGPlayer) null, options); } else {
			PlayerData dat = PlayerData.get(p);
			if (dat == null) { update((RPGPlayer) null, options); } else { update(dat.getRPG(), options); } }
	}
	/**
	 * This method updates the base stats of this item to what
	 * the template currently has, only rerolling RNGs where the
	 * probability of getting the same roll is less than 5%
	 * <p></p>
	 * Used when updating items with the updater.
	 *
	 * @param player There is an option where an item's base stats will be better
	 * 	             if the player who first generates the item is a higher level.
	 * 	             This player will be used to fulfill that operation.
	 *               <p></p>
	 *               If empty, it will use the old items level and tier,
	 *               or default values if needed.
	 *
	 * @see RevisionID
	 */
	@SuppressWarnings("ConstantConditions")
	public void update(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {

		// Initialize as Volatile, find source template. GemStones require a Live MMOItem though (to correctly load all Stat Histories and sh)
		loadLiveMMOItem();
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId()); ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (template == null) { MMOItems.print(null, "Could not find template for $r{0} {1}$b. ", "MMOItems Reforger", mmoItem.getType().toString(), mmoItem.getId()); mmoItem = null; return; }
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		// Skip all this trash and just regenerate completely
		if (options.isRegenerate()) {
			// Store all the history of stat proceedings.
			HashMap<ItemStat, StatHistory> temporalDataHistory = extractStatDataHistory(options);

			/*
			 * Generate fresh MMOItem, with stats that will be set if the chance is too low
			 */
			int determinedItemLevel = regenerate(player, template);
			//UPDT//MMOItems.log("Determined Level: \u00a7e" + determinedItemLevel);

			// Restore stats
			restorePreRNGStats(temporalDataHistory, template, determinedItemLevel);
			return; }

		/*
		 *   Has to store every stat into itemData, then check each stat of
		 *       the new item to see if they are RNG rolls in order to:
		 *
		 * 		1: If they arent, the probability of getting the old number
		 * 		   is straight up ZERO and must be replaced by the updated value.
		 *
		 * 		2: If they are RNG rolls, if the probability of getting the
		 * 		   current roll is at least 5%, the old roll is kept.
		 *
		 *      3: If the stat is gone completely, its again a ZERO chance
		 * 		   so it is removed (the updated value of 0 prevailing).
		 */
		// Keep name
		if (options.shouldKeepName()) { keepName(meta); }

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) { keepLore(); }

		EnchantListData ambiguouslyOriginalEnchantmentCache = null; //todo Corresponding to the block at the end of this method.
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) { Ref<EnchantListData> rf = new Ref<>(); keepEnchantments(rf); ambiguouslyOriginalEnchantmentCache = rf.getValue(); }

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) { keepUpgrades(); }

		// Gather Gemstones
		if (options.shouldKeepGemStones() || options.shouldKeepExternalSH()) { cacheFullHistory(!options.shouldKeepGemStones(), !options.shouldKeepExternalSH()); }

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) { keepSoulbound(); }

		// Store all the history of stat proceedings.
		HashMap<ItemStat, StatHistory> temporalDataHistory = extractStatDataHistory(options);

		/*
		 * Generate fresh MMOItem, with stats that will be set if the chance is too low
		 */
		int determinedItemLevel = regenerate(player, template);
		//UPDT//MMOItems.log("Determined Level: \u00a7e" + determinedItemLevel);

		// Restore stats
		restorePreRNGStats(temporalDataHistory, template, determinedItemLevel);

		// Choose enchantments to keep
		if (options.shouldKeepEnchantments() && ambiguouslyOriginalEnchantmentCache != null) { ambiguouslyOriginalEnchantmentCache.identifyTrueOriginalEnchantments(mmoItem, cachedEnchantments);}
	}

	@NotNull HashMap<ItemStat, StatHistory> extractStatDataHistory(@NotNull ReforgeOptions options) {
		HashMap<ItemStat, StatHistory> ret = new HashMap<>();

		//UPDT//MMOItems.log(" \u00a71*** \u00a77Remembering Stats");
		for (ItemStat stat : mmoItem.getStats()) {
			//UPDT//MMOItems.log(" \u00a79  * \u00a77Stat \u00a7f" + stat.getNBTPath());

			// Skip if it cant merge
			if (!(stat.getClearStatData() instanceof Mergeable)) { continue; }

			StatHistory hist = StatHistory.from(mmoItem, stat);
			//UPDT//MMOItems.log(" \u00a73  * \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath());

			// Clear externals
			if (!options.shouldKeepExternalSH()) { hist.getExternalData().clear(); }

			// Get and set
			ret.put(hist.getItemStat(), hist);
			//UPDT//MMOItems.log(" \u00a79  + \u00a77Storing for Update, History of \u00a7f" + hist.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + hist.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size());
			//GEM//MMOItems.log(" \u00a71  +-+ \u00a77Storing for Update, \u00a7bOG\u00a77: \u00a7f" + hist.getOriginalData().toString());
			//GEM//for (String str : SilentNumbers.transcribeList(hist.getAllGemstones(), (s) -> (s instanceof UUID ? hist.getGemstoneData((UUID) s) + "\u00a73 (\u00a78" + s + "\u00a73)" : "null"))) { MMOItems.log(" \u00a71  +-+ \u00a77Storing for Update, \u00a7bGM\u00a77: \u00a7f" + str); }
			//GEM//for (String str : SilentNumbers.transcribeList(hist.getExternalData(), (s) -> (s instanceof StatData ? s.toString() : "null"))) { MMOItems.log(" \u00a71  +-+ \u00a77Storing for Update, \u00a7bEX\u00a77: \u00a7f" + str); }
		}

		// Yes
		return ret;
	}

	@SuppressWarnings("ConstantConditions")
	void restorePreRNGStats(@NotNull HashMap<ItemStat, StatHistory> backup, @NotNull MMOItemTemplate template, int determinedItemLevel) {

		/*
		 * Extra step: Check every stat history
		 */
		int l = mmoItem.getUpgradeLevel();
		for (ItemStat stat : backup.keySet()) {
			//UPDT//MMOItems.log("\u00a7e @\u00a77 " + stat.getId());

			// Get history
			StatHistory hist = backup.get(stat);
			if (hist == null) { continue; }

			// Alr what the template say
			RandomStatData source = template.getBaseItemData().get(stat);
			StatHistory clear;

			/*
			 * Does the new item have it?
			 *
			 * If not, its gotten removed = we only keep extraneous
			 */
			if (source instanceof NumericStatFormula && hist.getOriginalData() instanceof DoubleData) {
				//UPDT//MMOItems.log("\u00a7a +\u00a77 Valid for Double Data procedure\u00a78 {Original:\u00a77 " + ((DoubleData) hist.getOriginalData()).getValue() + "\u00a78}");

				// Very well, chance checking is only available for NumericStatFormula class so
				double base = ((NumericStatFormula) source).getBase() + (((NumericStatFormula) source).getScale() * determinedItemLevel);

				// Determine current
				double current = ((DoubleData) hist.getOriginalData()).getValue();

				// What was the shift?
				double shift = current - base;

				// How many standard deviations away?
				double sD = Math.abs(shift / ((NumericStatFormula) source).getSpread());
				if (NumericStatFormula.useRelativeSpread) { sD = Math.abs(shift / (((NumericStatFormula) source).getSpread() * base)); }
				//UPDT//MMOItems.log("\u00a7b *\u00a77 Base: \u00a73" + base);
				//UPDT//MMOItems.log("\u00a7b *\u00a77 Curr: \u00a73" + current);
				//UPDT//MMOItems.log("\u00a7b *\u00a77 Shft: \u00a73" + shift);
				//UPDT//MMOItems.log("\u00a7b *\u00a77 SDev: \u00a73" + sD);

				// Greater than max spread? Or heck, 0.1% Chance or less wth
				if (sD > ((NumericStatFormula) source).getMaxSpread() || sD > 3.5) {
					//UPDT//MMOItems.log("\u00a7c -\u00a77 Ridiculous Range --- reroll");

					// Adapt within reason
					double reasonableShift = ((NumericStatFormula) source).getSpread() * Math.min(2, ((NumericStatFormula) source).getMaxSpread());
					if (shift < 0) { reasonableShift *= -1;}

					// That's the data we'll use
					DoubleData finalData = new DoubleData(reasonableShift + base);

					// Make a clear one
					clear = new StatHistory(mmoItem, stat, finalData);

					// Data arguably fine tbh, just use previous
				} else {
					//UPDT//MMOItems.log("\u00a7a +\u00a77 Acceptable Range --- kept");

					// Just clone I guess
					clear = new StatHistory(mmoItem, stat, ((DoubleData) hist.getOriginalData()).cloneData());
				}

			} else {
				//UPDT//MMOItems.log("\u00a7e +\u00a77 Not contained / unmerged --- reroll I suppose");

				/*
				 * These stats are exempt from this 'keeping' operation.
				 * Probably because there is a ReforgeOption specifically
				 * designed for them that keeps them separately
				 */
				if (ItemStats.LORE.equals(stat) ||
					ItemStats.NAME.equals(stat) ||
					ItemStats.GEM_SOCKETS.equals(stat)) {

					// Keep regenerated one
					clear = new StatHistory(mmoItem, stat, mmoItem.getData(stat));

				} else {

					// Make a clear one
					clear = new StatHistory(mmoItem, stat, hist.getOriginalData());
				}
			}

			// Keep Gemstone and Extraneous data
			for (UUID gem : hist.getAllGemstones()) { clear.registerGemstoneData(gem, hist.getGemstoneData(gem)); }
			for (StatData ex : hist.getExternalData()) { clear.registerExternalData(ex); }
			clear.setModifiersBonus(hist.getModifiersBonus());

			// Store
			itemDataHistory.put(stat, clear);
			mmoItem.setStatHistory(stat, clear);
			//HSY//MMOItems.log(" \u00a7b-\u00a7e- \u00a77Update Recalculation \u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-");
			//todo EXPERIMENTAL no longer recalculate here mmoItem.setData(stat, clear.recalculate(false, l));
		}
	}

	/**
	 * Creates a new item from the template itself
	 * @param p Player to roll modifies based on level
	 */
	void regenerate(@Nullable RPGPlayer p) {

		loadVolatileMMOItem();
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId()); ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (template == null) { MMOItems.print(null, "Could not find template for $r{0} {1}$b. ", "MMOItems Reforger", mmoItem.getType().toString(), mmoItem.getId()); mmoItem = null; return; }
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		//UPDT//MMOItems.log("\u00a79*\u00a77 Regenerating... \u00a7d" + template.getId() + " " + template.getType());

		if (p != null) {

			mmoItem = template.newBuilder(p).build();
		} else {

			mmoItem = template.newBuilder((mmoItem.hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) mmoItem.getData(ItemStats.ITEM_LEVEL)).getValue() : 0 ), null).build();
		}
	}
	/**
	 * Creates a new item from the template itself
	 * @param player Player to roll modifies based on level
	 * @param template Template to generate MMOItem from
	 */
	int regenerate(@Nullable RPGPlayer player, @NotNull MMOItemTemplate template) {

		int determinedItemLevel;
		if (player == null) {

			// Get default Item Level
			final int iLevel = defaultItemLevel;

			// What level with the regenerated item will be hmmmm.....
			determinedItemLevel =

					// No default level specified?
					(iLevel == -32767) ?

							// Does the item have level?
							(mmoItem.hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) mmoItem.getData(ItemStats.ITEM_LEVEL)).getValue() : 0 )

							// Default level was specified, use that.
							: iLevel;


			// Identify tier.
			ItemTier tier =

					// Does the item have a tier, and it should keep it?
					(keepTiersWhenReroll && mmoItem.hasData(ItemStats.TIER)) ?

							// The tier will be the current tier
							MMOItems.plugin.getTiers().get(mmoItem.getData(ItemStats.TIER).toString())

							// The item either has no tier, or shouldn't keep it. Null
							: null;

			// Build it again (Reroll RNG)
			mmoItem = template.newBuilder(determinedItemLevel, tier).build();

			// No player provided, use defaults.
		} else {

			// What level with the regenerated item will be hmmmm.....
			determinedItemLevel = (mmoItem.hasData(ItemStats.ITEM_LEVEL) ? (int) ((DoubleData) mmoItem.getData(ItemStats.ITEM_LEVEL)).getValue() : 0 );

			// Build it again (Reroll RNG)
			mmoItem = template.newBuilder(player).build();
		}

		return determinedItemLevel;
	}

	/**
	 * Generates a new item of the same Type-ID and transfers the data
	 * from the old one following the options.
	 *
	 * @param options Which data to 'keep'
	 * @param p There is an option where an item's base stats will be better
	 *          if the player who first generates the item is a higher level.
	 *          This player will be used to fulfill that operation.
	 *          <p></p>
	 *          If <code>null</code>, the default modifiers
	 *          specified in the config is used.
	 */
	public void reforge(@Nullable Player p, @NotNull ReforgeOptions options) {
		if (p == null) { reforge((RPGPlayer) null, options); } else {
			PlayerData dat = PlayerData.get(p);
			if (dat == null) { reforge((RPGPlayer) null, options); } else { reforge(dat.getRPG(), options); } } }
	/**
	 * Generates a new item of the same Type-ID and transfers the data
	 * from the old one following the options.
	 *
	 * @param player There is an option where an item's base stats will be better
	 * 	             if the player who first generates the item is a higher level.
	 * 	             This player will be used to fulfill that operation.
	 *               <p></p>
	 *               If empty, it will use the old items level and tier,
	 *               or default values if needed.
	 */
	@SuppressWarnings("ConstantConditions")
	public void reforge(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {
		if (options.isRegenerate()) { regenerate(player); return; }

		// Initialize as Volatile, find source template. GemStones require a Live MMOItem though (to correctly load all Stat Histories and sh)
		loadLiveMMOItem();
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId()); ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (template == null) { MMOItems.print(null, "Could not find template for $r{0} {1}$b. ", "MMOItems Reforger", mmoItem.getType().toString(), mmoItem.getId()); mmoItem = null; return; }
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		// Keep name
		if (options.shouldKeepName()) { keepName(meta);}

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) { keepLore(); }

		EnchantListData ambiguouslyOriginalEnchantmentCache = null; //todo Corresponding to the block at the end of this method.
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) { Ref<EnchantListData> rf = new Ref<>(); keepEnchantments(rf); ambiguouslyOriginalEnchantmentCache = rf.getValue(); }

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) { keepUpgrades(); }

		// Gather Gemstones
		if (options.shouldKeepGemStones() || options.shouldKeepExternalSH()) { cacheFullHistory(!options.shouldKeepGemStones(), !options.shouldKeepExternalSH()); }

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) { keepSoulbound(); }

		// Recreates this item from the template
		regenerate(player, template);

		// Choose enchantments to keep
		if (options.shouldKeepEnchantments() && ambiguouslyOriginalEnchantmentCache != null) { ambiguouslyOriginalEnchantmentCache.identifyTrueOriginalEnchantments(mmoItem, cachedEnchantments);}
	}

	/**
	 * Literally just copies the name onto the cached name
	 * @param meta Meta of the item uuuuh
	 */
	void keepName(@NotNull ItemMeta meta) {
		//UPDT//MMOItems.log(" \u00a73> \u00a77Keeping Name");

		// Does it have a name?
		if (mmoItem.hasData(ItemStats.NAME)) {

			// Cache it
			cachedName = mmoItem.getData(ItemStats.NAME).toString();

			// No name defined, use display name I guess (pretty unusual btw)
		} else if (meta.hasDisplayName()) {

			cachedName = meta.getDisplayName(); }

		//UPDT//MMOItems.log(" \u00a73  + \u00a77" + cachedName);
	}
	/**
	 * Examines the MMOItems Lore of the item being updated, and chooses which lore
	 * lines are desirable to keep (Those that start with §7)
	 */
	void keepLore() {
		if (!mmoItem.hasData(ItemStats.LORE)) { return; }
		cachedLore = extractLore(((StringListData) mmoItem.getData(ItemStats.LORE)).getList());
	}

	@NotNull ArrayList<String> extractLore(@NotNull List<String> lore) {

		//UPDT//MMOItems.log(" \u00a7d> \u00a77Keeping Lore");
		ArrayList<String> ret = new ArrayList<>();

		// Examine every element
		for (String str : lore) {
			//UPDT//MMOItems.log(" \u00a7d>\u00a7c-\u00a7e- \u00a77Line:\u00a7f " + str);

			// Does it start with the promised...?
			if (str.startsWith("\u00a77")) {
				//UPDT//MMOItems.log(" \u00a72>\u00a7a-\u00a7e- \u00a77Kept");
				ret.add(str); }
		}

		//UPDT//MMOItems.log(" \u00a7d> \u00a77Result");
		//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.log(" \u00a7d  + \u00a77" + lr); }
		return ret;
	}

	/**
	 *
	 * Step #1: Identify the current (not-null) enchantment data (creates one if missing)
	 *
	 * Step #2: Caches that data, and stores the current form as 'ambiguously original'
	 *
	 * Step #3: Completely merges cached enchantment with all extraneous enchantment list data in the History.
	 * 		    I don't remember why it does it, "As to not include it in this in the cached data later" is
	 * 		    what the comment says. Seems I had a reason to do it like that.
	 *
	 * @param ambiguouslyOriginalEnchantmentCache For now, we must keep a reference to the supposed 'Original Enchantment Data'
	 *                                            which may not be original (and thus contain player enchantments)
	 */
	void keepEnchantments(@NotNull Ref<EnchantListData> ambiguouslyOriginalEnchantmentCache) {
		//UPDT//MMOItems.log(" \u00a7b> \u00a77Keeping Enchantments");

		// Enchant list data
		cachedEnchantments = new EnchantListData();

		// Does it have MMOItems enchantment data?
		if (!mmoItem.hasData(ItemStats.ENCHANTS)) {
			//UPDT//MMOItems.log("  \u00a7b* \u00a77No Data, created blanc");
			mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData()); }
		//UPDT//else { MMOItems.log("  \u00a7b* \u00a77Found Data"); }

		// Make sure they are consolidated
		Enchants.separateEnchantments(mmoItem);

		// Gather
		StatHistory hist = StatHistory.from(mmoItem, ItemStats.ENCHANTS);
		ambiguouslyOriginalEnchantmentCache.setValue((EnchantListData) ((EnchantListData) hist.getOriginalData()).cloneData());

		//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Prime Arcane Report: \u00a7b-------------------------");
		//UPDT//MMOItems.log("  \u00a73> \u00a77History:");
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
		//UPDT//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
		//UPDT//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
		//UPDT//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }
		//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
		//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }
		//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
		//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getValue().getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getValue().getLevel(e)); }

		// Reap
		for (StatData pEnchants : hist.getExternalData()) {

			// It really should be but whatever
			if (pEnchants instanceof EnchantListData) {

				// Merge bruh
				((EnchantListData) pEnchants).merge(cachedEnchantments);

				// For every stat
				for (Enchantment e : ((EnchantListData) pEnchants).getEnchants()) {

					// Get result
					int calculated = ((EnchantListData) pEnchants).getLevel(e);

					// Put
					cachedEnchantments.addEnchant(e, calculated);
					//UPDT//MMOItems.log("  \u00a7b + \u00a77" + e.getName() + " " + calculated);
				}
			}
		}

		// The cache now stores the full extent of extraneous data. Separate from thy history. (As to not include it in this in the cached data later)
		hist.getExternalData().clear();

		//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Arcane Report: \u00a7b-------------------------");
		//UPDT//MMOItems.log("  \u00a73> \u00a77History:");
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
		//UPDT//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
		//UPDT//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
		//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
		//UPDT//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }
		//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
		//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }
		//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
		//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getValue().getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getValue().getLevel(e)); }
	}
	/**
	 * Just copies the upgrade data, with info on the level of course.
	 */
	void keepUpgrades() {
		//UPDT//MMOItems.log(" \u00a7e> \u00a77Keeping Upgrade Data");

		// Get Level
		cachedUpgradeLevel = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));
		//UPDT//MMOItems.log(" \u00a7e>-- \u00a77Level: \u00a7b" + (cachedUpgradeLevel != null ? cachedUpgradeLevel.getLevel() : "0 \u00a78(null)"));
	}
	/**
	 * Caches the full history of these items.
	 *
	 * @param clearGems Should ignore gemstones?
	 * @param clearExternalSH Should ignore external stat history?
	 */
	void cacheFullHistory(boolean clearGems, boolean clearExternalSH) {

		// Got any gem sockets bro?
		if (mmoItem.hasData(ItemStats.GEM_SOCKETS) && !clearGems) {

			//UPDT//MMOItems.log(" \u00a7a> \u00a77Keeping Gem Sockets Gx\u00a7b" + ((GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS)).getGemstones().size() + "\u00a77, Ex\u00a7b" + ((GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots().size());
			//GEM//for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+> \u00a77Gem: \u00a7a" + str); }
			//GEM//for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+> \u00a77Emp: \u00a76" + str); }

			// Cache that gemstone data
			cachedGemStones = (GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS); }

		//UPDT//MMOItems.log(" \u00a7a++ \u00a77Saving current histories (the information on current RNG rolls)");
		// Store all the history of stat proceedings.
		for (StatHistory hist : mmoItem.getStatHistories()) {
			//UPDT//MMOItems.log(" \u00a7a  + \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + hist.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size());

			// Clear externals
			if (clearExternalSH) {
				//UPDT//MMOItems.log(" \u00a7a  + \u00a77Clearing EXSH... \u00a7f");
				hist.getExternalData().clear(); }

			// Get and set
			itemDataHistory.put(hist.getItemStat(), hist);
			//UPDT//MMOItems.log(" \u00a7a  + \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + hist.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size());
			//GEM//MMOItems.log(" \u00a73  +-+ \u00a77OG: \u00a7f" + hist.getOriginalData().toString());
			//GEM//for (String str : SilentNumbers.transcribeList(hist.getAllGemstones(), (s) -> (s instanceof UUID ? hist.getGemstoneData((UUID) s) + "\u00a73 (\u00a78" + s + "\u00a73)" : "null"))) { MMOItems.log(" \u00a73  +-+ \u00a77GM: \u00a7f" + str); }
			//GEM//for (String str : SilentNumbers.transcribeList(hist.getExternalData(), (s) -> (s instanceof StatData ? s.toString() : "null"))) { MMOItems.log(" \u00a73  +-+ \u00a77EX: \u00a7f" + str); }
		}
	}
	/**
	 * Just copies the soulbound :B
	 */
	void keepSoulbound() {
		//UPDT//MMOItems.log(" \u00a7c> \u00a77Keeping Soulbind");

		// Find data
		cachedSoulbound = mmoItem.getData(ItemStats.SOULBOUND);
	}

	/**
	 * Was any reforge actually performed on this item?
	 */
	public boolean hasChanges() { return mmoItem != null; }

	/**
	 * Applies all the cached data on to the newly generated MMOItem, then builds
	 *
	 * @return Built item with changes applied
	 */
	public ItemStack toStack() {
		//UPDT//MMOItems.log(" \u00a7e@ \u00a73@ \u00a7a@ \u00a7d@ \u00a77Building Item Stack \u00a7e@ \u00a73@ \u00a7a@ \u00a7d@ ");
		MMOItem buildingMMOItem = mmoItem.clone();
		//LVL//MMOItems.log(" \u00a7d?\u00a77?\u00a76? \u00a77Lvl: \u00a7b" + buildingMMOItem.getUpgradeLevel());
		//GEM//MMOItems.log(" \u00a7a>0 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>0 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>0 \u00a77Emp: \u00a76" + str); }

		/*
		 * Upgrade Information
		 *
		 * It is important to do this before the Stat History Caches so that
		 * it knows to remember the original value of those stats that do get
		 * upgraded.
		 *
		 * (Without this, it thinks that the Original Data is clear and must
		 * not be included, since the upgrade hasn't been applied so it would
		 * be redundant to store the history tag)
		 */
		if (cachedUpgradeLevel != null) {

			//UPDT//MMOItems.log(" \u00a7e@ \u00a77Applying Upgrade");

			// If has a upgrade template defined, just remember the level
			if (buildingMMOItem.hasData(ItemStats.UPGRADE)) {
				//UPDT//MMOItems.log("  \u00a7e* \u00a77Existing Upgrade Detected");

				// Get current ig
				UpgradeData current = ((UpgradeData) buildingMMOItem.getData(ItemStats.UPGRADE));
				UpgradeData processed = new UpgradeData(current.getReference(), current.getTemplateName(), current.isWorkbench(), current.isDestroy(), current.getMax(), current.getSuccess());

				// Edit level
				processed.setLevel(Math.min(cachedUpgradeLevel.getLevel(), current.getMaxUpgrades()));
				//UPDT//MMOItems.log("  \u00a7e + \u00a77Set to level \u00a7f" + processed.getLevel() + " \u00a78Curr\u00a77 " + current.getLevel() + "\u00a78, Cache \u00a77" + cachedUpgradeLevel.getLevel());

				// Re-set cuz why not
				buildingMMOItem.setData(ItemStats.UPGRADE, processed); }

			// Actually decided that if the upgrade template is removed, server owner probably intends to clear upgrades so...
			/*else {
			    // New item has no Upgrade data? Then the old level AND template shall prevail
				//----//MMOItems.log("  \u00a7e* \u00a77Using Cached");

				// Set from the cached
				buildingMMOItem.setData(ItemStats.UPGRADE, new UpgradeData(cachedUpgradeLevel.getReference(), cachedUpgradeLevel.getTemplateName(), cachedUpgradeLevel.isWorkbench(), cachedUpgradeLevel.isDestroy(), cachedUpgradeLevel.getMax(), cachedUpgradeLevel.getSuccess()));
				//----//MMOItems.log("  \u00a7e + \u00a77Set to level \u00a7f" + cachedUpgradeLevel.getLevel());
			} //*/
		}
		//LVL//MMOItems.log(" \u00a7d?\u00a77?\u00a76? \u00a77Lvl: \u00a7b" + buildingMMOItem.getUpgradeLevel());

		// Apply histories
		int l = mmoItem.getUpgradeLevel();
		for (ItemStat stat : itemDataHistory.keySet()) {
			//UPDT//MMOItems.log(" \u00a72@\u00a76@ \u00a77Found Cached Stat History \u00a76" + stat.getId());
			//LVL//MMOItems.log(" \u00a7d?\u00a77?\u00a76? \u00a77Lvl: \u00a7b" + buildingMMOItem.getUpgradeLevel());

			// Does it have history too?
			StatHistory histOld = itemDataHistory.get(stat);
			if (histOld == null) {
				//UPDT//MMOItems.log(" \u00a72 *\u00a76* \u00a7cMissing");
				continue; }

			/*/ Is it completely clear?
			if (histOld.isClear()) {
				/UPDT//MMOItems.log(" \u00a72 *\u00a76* \u00a7cClear " + (histOld.getOriginalData() instanceof DoubleData ? "\u00a78 {Original:\u00a77 " + ((DoubleData) histOld.getOriginalData()).getValue() + "\u00a78}" : ""));
				continue; }
			/UPDT//MMOItems.log(" \u00a72 *\u00a76* \u00a7cNot clear: \u00a73Gems" + histOld.getAllGemstones().size() + " \u00a78|\u00a7b ExSH " + histOld.getExternalData().size() + (histOld.getOriginalData() instanceof DoubleData ? "\u00a78 {Original:\u00a77 " + ((DoubleData) histOld.getOriginalData()).getValue() + "\u00a78}" : ""));
			//*/

			// Regenerate the original data
			StatHistory hist = StatHistory.from(buildingMMOItem, stat);

			// Remember...
			hist.assimilate(histOld);

			// Recalculate
			//HSY//MMOItems.log(" \u00a7b-\u00a7e- \u00a77Reforging Prep Recalculation \u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-");
			buildingMMOItem.setData(hist.getItemStat(), hist.recalculate(false, l));

			//LVL//MMOItems.log(" \u00a7d?\u00a77?\u00a76? \u00a77Lvl: \u00a7b" + buildingMMOItem.getUpgradeLevel());
		}
		//GEM//MMOItems.log(" \u00a7a>1 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>1 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>1 \u00a77Emp: \u00a76" + str); }

		// Apply soulbound
		if (cachedSoulbound != null) {
			//UPDT//MMOItems.log(" \u00a7c@ \u00a77Applying Soulbind");

			// Apply
			buildingMMOItem.setData(ItemStats.SOULBOUND, cachedSoulbound);
		}

		// Contained enchantments huh
		if (cachedEnchantments != null) {
			//UPDT//MMOItems.log(" \u00a7b@ \u00a77Applying Enchantments");
			//UPDT//for (Enchantment lr : cachedEnchantments.getEnchants()) { MMOItems.log(" \u00a7b  + \u00a77" + lr.getName() + " \u00a7f" + cachedEnchantments.getLevel(lr)); }


			// Register as extraneous obviously
			StatHistory hist = StatHistory.from(buildingMMOItem, ItemStats.ENCHANTS);
			hist.registerExternalData(cachedEnchantments.cloneData());

			//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Late Arcane Report: \u00a79-------------------------");
			//UPDT//MMOItems.log("  \u00a73> \u00a77History:");
			//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
			//UPDT//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
			//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
			//UPDT//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
			//UPDT//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
			//UPDT//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }

			// Recalculate and put
			//HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Reforge Enchantments Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
			buildingMMOItem.setData(ItemStats.ENCHANTS, hist.recalculate(mmoItem.getUpgradeLevel()));
		}

		// Gem Stones
		if (cachedGemStones != null) {
			//UPDT//MMOItems.log(" \u00a7a@ \u00a77Applying Gemstones");
			ArrayList<GemstoneData> lostGems = new ArrayList<>();

			// If has a upgrade template defined, just remember the level
			if (buildingMMOItem.hasData(ItemStats.GEM_SOCKETS)) {
				//UPDT//MMOItems.log("  \u00a7a* \u00a77Existing Data Detected");

				// Get current ig
				GemSocketsData current = ((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS));

				// Get those damn empty sockets
				ArrayList<GemstoneData> putGems = new ArrayList<>();
				ArrayList<String> availableSockets = new ArrayList<>(current.getEmptySlots());
				ArrayList<GemstoneData> oldSockets = new ArrayList<>(cachedGemStones.getGemstones());

				// Remaining
				for (GemstoneData data : oldSockets) {
					//UPDT//MMOItems.log("  \u00a7a*\u00a7e* \u00a77Fitting \u00a7f" + data.getHistoricUUID().toString() + "\u00a77 '" + data.getName());

					// No more if no more sockets left
					if (availableSockets.size() <= 0) {
						//UPDT//MMOItems.log(" \u00a7a  +\u00a7c+ \u00a77No More Sockets");

						// This gemstone could not be inserted, it is thus lost
						lostGems.add(data);
						//UPDT//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno socket \u00a78" + data.getHistoricUUID());

					// Still some sockets to fill hMMM
					} else {

						// Get colour, uncolored if Unknown
						String colour = data.getSocketColor();
						if (colour == null) { colour = GemSocketsData.getUncoloredGemSlot(); }
						String remembrance = null;

						// Does the gem data have an available socket?
						for (String slot : availableSockets) { if (slot.equals(GemSocketsData.getUncoloredGemSlot()) || colour.equals(slot)) { remembrance = slot; } }

						// Existed?
						if (remembrance != null) {
							//UPDT//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone fit - \u00a7e " + remembrance + " \u00a78" + data.getHistoricUUID());

							// Remove
							availableSockets.remove(remembrance);

							// And guess what... THAT is the colour of this gem! Fabulous huh?
							data.setColour(remembrance);

							// Remember as a put gem
							putGems.add(data);

						// No space/valid socket hmm
						} else {
							//UPDT//MMOItems.log("\u00a7c *\u00a7e*\u00a77 Gemstone lost - \u00a7cno color \u00a78" + data.getHistoricUUID());

							// Include as lost gem
							lostGems.add(data); }
					}
				}

				// Create with select socket slots and gems
				GemSocketsData primeGems = new GemSocketsData(availableSockets);
				for (GemstoneData gem : putGems) { if (gem == null) { continue; } primeGems.add(gem); }

				// That's the original data
				StatHistory gemStory = StatHistory.from(buildingMMOItem, ItemStats.GEM_SOCKETS);
				gemStory.setOriginalData(primeGems);

				//HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Restore Gemstones Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
				buildingMMOItem.setData(ItemStats.GEM_SOCKETS, gemStory.recalculate(l));

			// Could not fit any gems: No gem sockets!
			} else {
				//UPDT//MMOItems.log("\u00a7c *\u00a7e*\u00a77 All gemstones were lost -  \u00a7cno data");

				// ALl were lost
				lostGems.addAll(cachedGemStones.getGemstones()); }

			// Config option enabled? Build the lost gem MMOItems!
			if (ReforgeOptions.dropRestoredGems) {
				for (GemstoneData lost : lostGems) {

					// Get MMOItem
					MMOItem restoredGem = buildingMMOItem.extractGemstone(lost);

					// Success?
					if (restoredGem != null) { destroyedGems.add(restoredGem); } } }
		}
		//GEM//MMOItems.log(" \u00a7a>\u00a7e2 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>\u00a7e2 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>\u00a7e2 \u00a77Emp: \u00a76" + str); }

		// Lore
		if (!cachedLore.isEmpty()) {
			//UPDT//MMOItems.log(" \u00a7d@ \u00a77Applying Lore");

			// If it has lore, add I guess
			if (buildingMMOItem.hasData(ItemStats.LORE)) {
				//UPDT//MMOItems.log("  \u00a7d* \u00a77Inserting first");

				// Get current ig
				StringListData current = ((StringListData) buildingMMOItem.getData(ItemStats.LORE));

				// Get those damn empty sockets
				ArrayList<String> listYes = new ArrayList<>(current.getList());

				// Append to the end of the cached >:]
				cachedLore.addAll(listYes);
			}

			// Create stat
			StringListData sData = new StringListData(cachedLore);
			//UPDT//for (String lr : cachedLore) { MMOItems.log(" \u00a7d  + \u00a77" + lr); }

			// Set that as the lore
			buildingMMOItem.setData(ItemStats.LORE, sData);
		}

		// Name
		if (cachedName != null) {
			//UPDT//MMOItems.log(" \u00a73@ \u00a77Applying Name \u00a7f" + cachedName);

			// Replace name completely
			buildingMMOItem.setData(ItemStats.NAME, new StringData(cachedName));
		}

		// Apply upgrades
		if (buildingMMOItem.hasUpgradeTemplate()) {
			//UPDT//MMOItems.log(" \u00a7f@ \u00a77Recalculating Upgrades");

			buildingMMOItem.getUpgradeTemplate().upgradeTo(buildingMMOItem, buildingMMOItem.getUpgradeLevel()); }
		//GEM//MMOItems.log(" \u00a7a>3 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>3 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>3 \u00a77Emp: \u00a76" + str); }

		// Build and set amount
		ItemStack stack = buildingMMOItem.newBuilder().build();
		stack.setAmount(amount);
		return stack;
	}

	/*
	 * Initialize the MMOItem as a LiveMMOItem if it's null or not already a
	 * LiveMMOItem
	 */
	private void loadLiveMMOItem() {
		if (mmoItem != null && mmoItem instanceof LiveMMOItem) { return; }
		mmoItem = new LiveMMOItem(nbtItem);
	}

	/* Initialize the MMOItem as a VolatileMMOItem if it's null */
	private void loadVolatileMMOItem() {
		if (mmoItem != null) { return;}
		mmoItem = new VolatileMMOItem(nbtItem);
	}

	@NotNull ArrayList<MMOItem> destroyedGems = new ArrayList<>();

	/**
	 * @return List of gems that have been destroyed from an item, presumably due
	 * 		   to updating it without keeping gemstones, or they simply could not
	 * 		   fit.
	 * 		   <br>
	 * 		   Currently, it is only ever filled if the config option drop-gems
	 * 		   for item revision is enabled.
	 */
	@NotNull public ArrayList<MMOItem> getDestroyedGems() { return destroyedGems; }
}
