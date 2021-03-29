package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
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
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
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
	static boolean rerollWhenUpdated = false;
	static boolean keepTiersWhenReroll = true;
	
	public static void reload() {
		autoSoulboundLevel = MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1);
		rerollWhenUpdated = MMOItems.plugin.getConfig().getBoolean("item-revision.reroll-when-updated", false);
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
		update(p == null ? null : PlayerData.get(p).getRPG(), options);
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
	public void update(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {

		// Should it re-roll RNG?
		if (rerollWhenUpdated) {

			// Reroute to Reforge
			reforge(player, options);
			return;
		}

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

		// Initialize as Volatile, find source template. GemStones require a Live MMOItem though (to correctly load all Stat Histories and sh)
		if (!options.shouldKeepGemStones() && !options.shouldKeepExternalSH()) { loadVolatileMMOItem(); } else { loadLiveMMOItem(); }
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId()); ItemMeta meta = nbtItem.getItem().getItemMeta();
		//noinspection ConstantConditions
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		// Keep name
		if (options.shouldKeepName()) {
			//UPDT//MMOItems.log(" \u00a73> \u00a77Keeping Name");

			// Does it have a name?
			if (mmoItem.hasData(ItemStats.NAME)) {

				// Cache it
				cachedName = mmoItem.getData(ItemStats.NAME).toString();

				// No name defined, use display name I guess (pretty unusual btw)
			} else if (meta.hasDisplayName()) {

				cachedName = meta.getDisplayName();
			}

			//UPDT//MMOItems.log(" \u00a73  + \u00a77" + cachedName);
		}

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) {
			//UPDT//MMOItems.log(" \u00a7d> \u00a77Keeping Lore");

			// Examine every element
			for (String str : ((StringListData) mmoItem.getData(ItemStats.LORE)).getList()) {

				// Does it start with the promised...?
				if (str.startsWith("\u00a77")) { cachedLore.add(str); }
			}

			//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.log(" \u00a7d  + \u00a77" + lr); }
		}

		EnchantListData ambiguouslyOriginalEnchantmentCache = null; //todo Corresponding to the block at the end of this method.
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) {
			//UPDT//MMOItems.log(" \u00a7b> \u00a77Keeping Enchantments");

			// Enchant list data
			cachedEnchantments = new EnchantListData();

			// Does it have MMOItems enchantment data?
			if (!mmoItem.hasData(ItemStats.ENCHANTS)) {
				//UPDT//MMOItems.log("  \u00a7b* \u00a77No Data");
				mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());

				// Nope
			}
			//UPDT//else { MMOItems.log("  \u00a7b* \u00a77Found Data"); }

			// Make sure they are consolidated
			Enchants.separateEnchantments(mmoItem);

			// Gather
			StatHistory hist = StatHistory.from(mmoItem, ItemStats.ENCHANTS);
			ambiguouslyOriginalEnchantmentCache = (EnchantListData) ((EnchantListData) hist.getOriginalData()).cloneData();


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
			//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }


			// Reap
			for (StatData pEnchants : hist.getExternalData()) {

				// It really should be but whatever
				if (pEnchants instanceof EnchantListData) {

					// For every stat
					for (Enchantment e : ((EnchantListData) pEnchants).getEnchants()) {

						// Get Base/Current
						int established = cachedEnchantments.getLevel(e);

						// Add
						int calculated = established + ((EnchantListData) pEnchants).getLevel(e);

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
			//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

		}

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) {
			//UPDT//MMOItems.log(" \u00a7e> \u00a77Keeping Upgrade Data");

			// Get Level
			cachedUpgradeLevel = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));
		}

		// Gather Gemstones
		if (options.shouldKeepGemStones() && mmoItem.hasData(ItemStats.GEM_SOCKETS)) {

				//UPDT//MMOItems.log(" \u00a7a> \u00a77Keeping Gem Sockets");

				// Cache that gemstone data
				cachedGemStones = (GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS);
		}

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) {
			//UPDT//MMOItems.log(" \u00a7c> \u00a77Keeping Soulbind");

			// Find data
			cachedSoulbound = mmoItem.getData(ItemStats.SOULBOUND);
		}

		// Store all the history of stat proceedings.
		HashMap<ItemStat, StatHistory> temporalDataHistory = new HashMap<>();
		for (StatHistory hist : mmoItem.getStatHistories()) {
			//UPDT//MMOItems.log(" \u00a7a  + \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath());

			// Clear externals
			if (!options.shouldKeepExternalSH()) { hist.getExternalData().clear(); }

			// Get and set
			temporalDataHistory.put(hist.getItemStat(), hist);
		}

		/*
		 * Generate fresh MMOItem, with stats that will be set if the chance is too low
		 */
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

		/*
		 * Extra step: Check every stat history
		 */
		for (ItemStat stat : temporalDataHistory.keySet()) {

			// Get history
			StatHistory hist = temporalDataHistory.get(stat);
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

				// Very well, chance checking is only available for NumericStatFormula class so
				double base = ((NumericStatFormula) source).getBase() + (((NumericStatFormula) source).getScale() * determinedItemLevel);

				// Determine current
				double current = ((DoubleData) hist.getOriginalData()).getValue();

				// What was the shift?
				double shift = current - base;

				// How many standard deviations away?
				double sD = Math.abs(shift / ((NumericStatFormula) source).getSpread());
				if (NumericStatFormula.useRelativeSpread) { sD = Math.abs(shift / (((NumericStatFormula) source).getSpread() * base)); }

				// Greater than max spread? Or heck, 0.1% Chance or less wth
				if (sD > ((NumericStatFormula) source).getMaxSpread() || sD > 3.5) {

					// Adapt within reason
					double reasonableShift = ((NumericStatFormula) source).getSpread() * Math.min(2, ((NumericStatFormula) source).getMaxSpread());
					if (shift < 0) { reasonableShift *= -1;}

					// That's the data we'll use
					DoubleData finalData = new DoubleData(reasonableShift + base);

					// Make a clear one
					clear = new StatHistory(mmoItem, stat, finalData);

				// Data arguably fine tbh, just use previous
				} else {

					// Just clone I guess
					clear = new StatHistory(mmoItem, stat, ((DoubleData) hist.getOriginalData()).cloneData());
				}

			} else {

				// Make a clear one
				clear = new StatHistory(mmoItem, stat, stat.getClearStatData());
			}

			// Keep Gemstone and Extraneous data
			for (UUID gem : hist.getAllGemstones()) { clear.registerGemstoneData(gem, hist.getGemstoneData(gem)); }
			for (StatData ex : hist.getExternalData()) { clear.registerExternalData(ex); }

			// Store
			itemDataHistory.put(stat, clear);
		}

		/*
		 *  todo We cannot yet assume (for a few months) that the Original Enchantment Data
		 *   registered into the Stat History is actually true to the template (since it may
		 *   be the enchantments of an old-enchanted item, put by the player).
		 *   _
		 *   Thus this block of code checks the enchantment data of the newly generated
		 *   MMOItem and follows the following logic to give our best guess if the Original
		 *   stats are actually Original:
		 *
		 *   1: Is the item unenchantable and unrepairable? Then they must be original
		 *
		 *   2: Does the template have no enchantments? Then they must be external
		 *
		 *   3: Does the template have this enchantment at an unobtainable level? Then it must be original
		 *
		 *   4: Does the template have this enchantment at a lesser level? Then it must be external (player upgraded it)
		 *
		 *   Original: Included within the template at first creation
		 *   External: Enchanted manually by a player
		 *
		 */
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments() && ambiguouslyOriginalEnchantmentCache != null) {
			//UPDT//MMOItems.log(" \u00a7b> \u00a77Original Enchantments Upkeep");

			// 1: The item is unenchantable and unrepairable? Cancel this operation, the cached are Original
			if (mmoItem.hasData(ItemStats.DISABLE_ENCHANTING) && mmoItem.hasData(ItemStats.DISABLE_REPAIRING)) {
				//UPDT//MMOItems.log(" \u00a7bType-1 \u00a77Original Identification");

				ambiguouslyOriginalEnchantmentCache.clear();

				//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
				//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
				//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

				//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
				//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

				return;
			}
			if (!mmoItem.hasData(ItemStats.ENCHANTS)) { mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());}

			// 2: If it has data (It always has) and the amount of enchants is zero, the cached are Extraneous
			if (((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getEnchants().size() == 0) {
				//UPDT//MMOItems.log(" \u00a73Type-2 \u00a77Extraneous Identification");

				// All right, lets add those to cached enchantments
				cachedEnchantments.merge(ambiguouslyOriginalEnchantmentCache);

				//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
				//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
				//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

				//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
				//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

				return;
			}

			// Which enchantments are deemed external, after all?
			EnchantListData processed = new EnchantListData();

			// Identify material
			mmoItem.hasData(ItemStats.MATERIAL); MaterialData mData = (MaterialData) mmoItem.getData(ItemStats.MATERIAL); Material mat = mData.getMaterial();

			// 3 & 4: Lets examine every stat
			for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) {
				//UPDT//MMOItems.log(" \u00a7b  = \u00a77Per Enchant - \u00a7f" + e.getName());

				// Lets see hmm
				int current = ambiguouslyOriginalEnchantmentCache.getLevel(e);
				int updated = ((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getLevel(e);
				//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Current \u00a7f" + current);
				//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Updated \u00a7f" + updated);

				// 3: Is it at an unobtainable level? Then its Original
				if (updated > e.getMaxLevel() || !e.getItemTarget().includes(mat)) {
					//UPDT//MMOItems.log(" \u00a7bType-3 \u00a77Original Identification");

					continue;
				}

				// 4: Is it at a lesser level? Player must have enchanted, take them as External
				if (updated < current) {
					//UPDT//MMOItems.log(" \u00a73Type-4 \u00a77Extraneous Identification");
					processed.addEnchant(e, current);
					//noinspection UnnecessaryContinue
					continue;
				}

				//UPDT//MMOItems.log(" \u00a73Type-5 \u00a77Original Identification");
			}

			// All right, lets add those to cached enchantments
			cachedEnchantments.merge(processed);

			//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
			//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
			//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

			//UPDT//MMOItems.log("  \u00a73> \u00a77Processed:");
			//UPDT//for (Enchantment e : processed.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + processed.getLevel(e)); }
		}
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
		reforge(p == null ? null : PlayerData.get(p).getRPG(), options);
	}

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
	public void reforge(@Nullable RPGPlayer player, @NotNull ReforgeOptions options) {

		// Initialize as Volatile, find source template. GemStones require a Live MMOItem though (to correctly load all Stat Histories and sh)
		if (!options.shouldKeepGemStones() && !options.shouldKeepExternalSH()) { loadVolatileMMOItem(); } else { loadLiveMMOItem(); }
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId()); ItemMeta meta = nbtItem.getItem().getItemMeta();
		//noinspection ConstantConditions
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		// Keep name
		if (options.shouldKeepName()) {
			//UPDT//MMOItems.log(" \u00a73> \u00a77Keeping Name");

			// Does it have a name?
			if (mmoItem.hasData(ItemStats.NAME)) {

				// Cache it
				cachedName = mmoItem.getData(ItemStats.NAME).toString();

			// No name defined, use display name I guess (pretty unusual btw)
			} else if (meta.hasDisplayName()) {

				cachedName = meta.getDisplayName();
			}

			//UPDT//MMOItems.log(" \u00a73  + \u00a77" + cachedName);
		}

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) {
			//UPDT//MMOItems.log(" \u00a7d> \u00a77Keeping Lore");

			// Examine every element
			for (String str : ((StringListData) mmoItem.getData(ItemStats.LORE)).getList()) {

				// Does it start with the promised...?
				if (str.startsWith("\u00a77")) { cachedLore.add(str); }
			}

			//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.log(" \u00a7d  + \u00a77" + lr); }
		}

		EnchantListData ambiguouslyOriginalEnchantmentCache = null; //todo Corresponding to the block at the end of this method.
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) {
			//UPDT//MMOItems.log(" \u00a7b> \u00a77Keeping Enchantments");

			// Enchant list data
			cachedEnchantments = new EnchantListData();

			// Does it have MMOItems enchantment data?
			if (!mmoItem.hasData(ItemStats.ENCHANTS)) {
				//UPDT//MMOItems.log("  \u00a7b* \u00a77No Data");
				mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());

			// Nope
			}
			//UPDT//else { MMOItems.log("  \u00a7b* \u00a77Found Data"); }

			// Make sure they are consolidated
			Enchants.separateEnchantments(mmoItem);

			// Gather
			StatHistory hist = StatHistory.from(mmoItem, ItemStats.ENCHANTS);
			ambiguouslyOriginalEnchantmentCache = (EnchantListData) ((EnchantListData) hist.getOriginalData()).cloneData();


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
			//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }


			// Reap
			for (StatData pEnchants : hist.getExternalData()) {

				// It really should be but whatever
				if (pEnchants instanceof EnchantListData) {

					// For every stat
					for (Enchantment e : ((EnchantListData) pEnchants).getEnchants()) {

						// Get Base/Current
						int established = cachedEnchantments.getLevel(e);

						// Add
						int calculated = established + ((EnchantListData) pEnchants).getLevel(e);

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
			//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

		}

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) {
			//UPDT//MMOItems.log(" \u00a7e> \u00a77Keeping Upgrade Data");

			// Get Level
			cachedUpgradeLevel = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));
		}

		// Gather Gemstones
		if (options.shouldKeepGemStones() || options.shouldKeepExternalSH()) {

			// Got any gem sockets bro?
			if (mmoItem.hasData(ItemStats.GEM_SOCKETS) && options.shouldKeepGemStones()) {

				//UPDT//MMOItems.log(" \u00a7a> \u00a77Keeping Gem Sockets");

				// Cache that gemstone data
				cachedGemStones = (GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS); }

			// Store all the history of stat proceedings.
			for (StatHistory hist : mmoItem.getStatHistories()) {
				//UPDT//MMOItems.log(" \u00a7a  + \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath());

				// Clear externals
				if (!options.shouldKeepExternalSH()) { hist.getExternalData().clear(); }

				// Get and set
				itemDataHistory.put(hist.getItemStat(), hist);
			}
		}

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) {
			//UPDT//MMOItems.log(" \u00a7c> \u00a77Keeping Soulbind");

			// Find data
			cachedSoulbound = mmoItem.getData(ItemStats.SOULBOUND);
		}

		if (player == null) {

			// Get default Item Level
			final int iLevel = defaultItemLevel;

			// What level with the regenerated item will be hmmmm.....
			int level =

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
			mmoItem = template.newBuilder(level, tier).build();

		// No player provided, use defaults.
		} else {

			// Build it again (Reroll RNG)
			mmoItem = template.newBuilder(player).build();
		}

		/*
		 *  todo We cannot yet assume (for a few months) that the Original Enchantment Data
		 *   registered into the Stat History is actually true to the template (since it may
		 *   be the enchantments of an old-enchanted item, put by the player).
		 *   _
		 *   Thus this block of code checks the enchantment data of the newly generated
		 *   MMOItem and follows the following logic to give our best guess if the Original
		 *   stats are actually Original:
		 *
		 *   1: Is the item unenchantable and unrepairable? Then they must be original
		 *
		 *   2: Does the template have no enchantments? Then they must be external
		 *
		 *   3: Does the template have this enchantment at an unobtainable level? Then it must be original
		 *
		 *   4: Does the template have this enchantment at a lesser level? Then it must be external (player upgraded it)
		 *
		 *   Original: Included within the template at first creation
		 *   External: Enchanted manually by a player
		 *
		 */
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments() && ambiguouslyOriginalEnchantmentCache != null) {
			//UPDT//MMOItems.log(" \u00a7b> \u00a77Original Enchantments Upkeep");

			// 1: The item is unenchantable and unrepairable? Cancel this operation, the cached are Original
			if (mmoItem.hasData(ItemStats.DISABLE_ENCHANTING) && mmoItem.hasData(ItemStats.DISABLE_REPAIRING)) {
				//UPDT//MMOItems.log(" \u00a7bType-1 \u00a77Original Identification");

				ambiguouslyOriginalEnchantmentCache.clear();

				//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
				//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
				//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

				//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
				//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

				return;
			}
			if (!mmoItem.hasData(ItemStats.ENCHANTS)) { mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());}

			// 2: If it has data (It always has) and the amount of enchants is zero, the cached are Extraneous
			if (((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getEnchants().size() == 0) {
				//UPDT//MMOItems.log(" \u00a73Type-2 \u00a77Extraneous Identification");

				// All right, lets add those to cached enchantments
				cachedEnchantments.merge(ambiguouslyOriginalEnchantmentCache);

				//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
				//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
				//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

				//UPDT//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
				//UPDT//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getLevel(e)); }

				return;
			}

			// Which enchantments are deemed external, after all?
			EnchantListData processed = new EnchantListData();

			// Identify material
			mmoItem.hasData(ItemStats.MATERIAL); MaterialData mData = (MaterialData) mmoItem.getData(ItemStats.MATERIAL); Material mat = mData.getMaterial();

			// 3 & 4: Lets examine every stat
			for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getEnchants()) {
				//UPDT//MMOItems.log(" \u00a7b  = \u00a77Per Enchant - \u00a7f" + e.getName());

				// Lets see hmm
				int current = ambiguouslyOriginalEnchantmentCache.getLevel(e);
				int updated = ((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getLevel(e);
				//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Current \u00a7f" + current);
				//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Updated \u00a7f" + updated);

				// 3: Is it at an unobtainable level? Then its Original
				if (updated > e.getMaxLevel() || !e.getItemTarget().includes(mat)) {
					//UPDT//MMOItems.log(" \u00a7bType-3 \u00a77Original Identification");

					continue;
				}

				// 4: Is it at a lesser level? Player must have enchanted, take them as External
				if (updated < current) {
					//UPDT//MMOItems.log(" \u00a73Type-4 \u00a77Extraneous Identification");
					processed.addEnchant(e, current);
					//noinspection UnnecessaryContinue
					continue;
				}

				//UPDT//MMOItems.log(" \u00a73Type-5 \u00a77Original Identification");
			}

			// All right, lets add those to cached enchantments
			cachedEnchantments.merge(processed);

			//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
			//UPDT//MMOItems.log("  \u00a73> \u00a77Cached:");
			//UPDT//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }

			//UPDT//MMOItems.log("  \u00a73> \u00a77Processed:");
			//UPDT//for (Enchantment e : processed.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + processed.getLevel(e)); }
		}
	}

	/**
	 * Was any reforge actually performed on this item?
	 */
	public boolean hasChanges() { return mmoItem != null; }

	public ItemStack toStack() {
		MMOItem buildingMMOItem = mmoItem.clone();

		// Apply histories
		for (ItemStat stat : itemDataHistory.keySet()) {
			//UPDT//MMOItems.log(" \u00a72@\u00a76@ \u00a77Cached Stat History");

			// Does it have history too?
			StatHistory histOld = itemDataHistory.get(stat);
			if (histOld != null) {
				//UPDT//MMOItems.log(" \u00a72 *\u00a76* \u00a77Of old stat \u00a7f" + histOld.getItemStat().getNBTPath());

				// Regenerate the original data
				StatHistory hist = StatHistory.from(buildingMMOItem, stat);

				// Remember...
				hist.assimilate(histOld);

				// Recalculate
				buildingMMOItem.setData(hist.getItemStat(), hist.recalculate(false));
			}
		}

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
			buildingMMOItem.setData(ItemStats.ENCHANTS, hist.recalculate());
		}

		// Upgrade Information
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
				//UPDT//MMOItems.log("  \u00a7e + \u00a77Set to level \u00a7f" + current.getLevel());

				// Re-set cuz why not
				buildingMMOItem.setData(ItemStats.UPGRADE, processed);

			// Otherwise, the level AND template shall prevail
			}

			/*else {
				//UPDT//MMOItems.log("  \u00a7e* \u00a77Using Cached");

				// Set from the cached
				buildingMMOItem.setData(ItemStats.UPGRADE, new UpgradeData(cachedUpgradeLevel.getReference(), cachedUpgradeLevel.getTemplateName(), cachedUpgradeLevel.isWorkbench(), cachedUpgradeLevel.isDestroy(), cachedUpgradeLevel.getMax(), cachedUpgradeLevel.getSuccess()));
				//UPDT//MMOItems.log("  \u00a7e + \u00a77Set to level \u00a7f" + cachedUpgradeLevel.getLevel());
			} //*/
		}

		// Gem Stones
		if (cachedGemStones != null) {
			//UPDT//MMOItems.log(" \u00a7a@ \u00a77Applying Gemstones");

			// If has a upgrade template defined, just remember the level
			if (buildingMMOItem.hasData(ItemStats.GEM_SOCKETS)) {
				//UPDT//MMOItems.log("  \u00a7a* \u00a77Existing Data Detected");

				// Get current ig
				GemSocketsData current = ((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS));

				// Get those damn empty sockets
				ArrayList<String> availableSockets = new ArrayList<>(current.getEmptySlots());
				ArrayList<GemstoneData> oldSockets = new ArrayList<>(cachedGemStones.getGemstones());

				// Remaining
				for (GemstoneData data : oldSockets) {
					//UPDT//MMOItems.log("  \u00a7a*\u00a7e* \u00a77Fitting \u00a7f" + data.getHistoricUUID().toString());

					// No more if no more sockets left
					if (availableSockets.size() <= 0) {
						//UPDT//MMOItems.log(" \u00a7a  +\u00a7c+ \u00a77No More Sockets");

						// They all will fit anyway
						break;

					// Still some sockets to fill hMMM
					} else {

						// Get colour
						String colour = data.getColour();
						String remembrance;

						// Not null?
						if (colour != null) {

							// Contained? Remove
							remembrance = colour;

					 	// No colour data, just remove a random slot ig
						} else {

							// Get and remove
							remembrance = availableSockets.get(0);
						}

						// Remove
						availableSockets.remove(remembrance);

						// And guess what... THAT is the colour of this gem! Fabulous huh?
						data.setColour(remembrance);
						//UPDT//MMOItems.log(" \u00a7a  + \u00a77Fit into color \u00a7f" + remembrance);
					}
				}

				// Update list of empty sockets
				cachedGemStones.getEmptySlots().clear();
				cachedGemStones.getEmptySlots().addAll(availableSockets);
			}

			// Set the data, as changed as it may be
			buildingMMOItem.setData(ItemStats.GEM_SOCKETS, cachedGemStones);
		}

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
			//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.log(" \u00a7d  + \u00a77" + lr); }

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
		if (buildingMMOItem.hasUpgradeTemplate()) { buildingMMOItem.getUpgradeTemplate().upgradeTo(buildingMMOItem, buildingMMOItem.getUpgradeLevel()); }

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
}
