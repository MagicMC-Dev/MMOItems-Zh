package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.Type;
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
import net.Indyuce.mmoitems.stat.data.random.UpdatableRandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.NameData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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
	@Nullable
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
	@Nullable StatData cachedSoulbound;

	//endregion

	/**
	 *  Prepare to reforge this MMOItem (starts out as NBTItem due to backend reasons).
	 *  @param nbt <b>Make sure {@link NBTItem#hasType()} returns true for this</b>.
	 */
	public MMOItemReforger(@NotNull NBTItem nbt) {
		this.nbtItem = nbt;
		this.amount = nbt.getItem().getAmount();

		// Spec the name and ID
		VolatileMMOItem vol = new VolatileMMOItem(nbt);
		miTypeName = vol.getType();
		miID = vol.getId();

		// Attempt to cache durability
		if (vol.hasData(ItemStats.DURABILITY)) { cachedDurability = (DoubleData) vol.getData(ItemStats.DURABILITY); }
		else if (nbt.getItem().getItemMeta() instanceof Damageable) { cachedDur = ((double) (((Damageable) nbt.getItem().getItemMeta()).getDamage())) / ((double) nbt.getItem().getType().getMaxDurability()); }
	}
	@NotNull final String miID;
	@NotNull final Type miTypeName;
	@Nullable DoubleData cachedDurability = null;
	@Nullable Double cachedDur = null;

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
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(miTypeName, miID); ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (template == null) { MMOItems.print(null, "Could not find template for $r{0} {1}$b. ", "MMOItems Reforger", miTypeName.toString(), miID); mmoItem = null; return; }
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));


		// Skip all this trash and just regenerate completely
		if (options.isRegenerate()) {
			// Store all the history of stat proceedings.
			HashMap<ItemStat, StatHistory> temporalDataHistory = extractStatDataHistory();

			/*
			 * Generate fresh MMOItem, with stats that will be set if the chance is too low
			 */
			int determinedItemLevel = regenerate(player, template);
			//UPGRD//MMOItems.log("Determined Level: \u00a7a" + determinedItemLevel + " \u00a78{ Regeneration }");

			// Restore stats
			restorePreRNGStats(temporalDataHistory, template, determinedItemLevel, true);
			return; }

		// Load live
		loadLiveMMOItem();

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
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) { keepLore(options.getKeepCase()); }

		EnchantListData ambiguouslyOriginalEnchantmentCache = null;
		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) { Ref<EnchantListData> rf = new Ref<>(); keepEnchantments(rf); ambiguouslyOriginalEnchantmentCache = rf.getValue(); }

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) { keepUpgrades(); }

		// Gather Gemstones and ExSH in a Stat History clone.
		if (options.shouldKeepGemStones() || options.shouldKeepExternalSH()) { cacheFullHistory(!options.shouldKeepGemStones(), !options.shouldKeepExternalSH()); }

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) { keepSoulbound(); }

		// Store all the history of stat proceedings. This clears ExSH and Gems so they must already be remembered.
		HashMap<ItemStat, StatHistory> temporalDataHistory = extractStatDataHistory();

		/*
		 * Generate fresh MMOItem, with stats that will be set if the chance is too low
		 */
		int determinedItemLevel = regenerate(player, template);
		//UPGRD//MMOItems.log("Determined Level: \u00a7e" + determinedItemLevel);

		// Restore stats, handles Original Data and Modifiers.
		restorePreRNGStats(temporalDataHistory, template, determinedItemLevel, false);

		// Choose enchantments to keep
		if (options.shouldKeepEnchantments() && ambiguouslyOriginalEnchantmentCache != null) { ambiguouslyOriginalEnchantmentCache.identifyTrueOriginalEnchantments(mmoItem, cachedEnchantments);}
	}

	@NotNull HashMap<ItemStat, StatHistory> extractStatDataHistory() {
		HashMap<ItemStat, StatHistory> ret = new HashMap<>();

		//UPGRD//MMOItems.log(" \u00a71*** \u00a77Extracting History Data prior to RNG Reroll");
		for (ItemStat stat : mmoItem.getStats()) {
			//UPGRD//MMOItems.log(" \u00a79  * \u00a77Stat \u00a7f" + stat.getNBTPath());

			// Skip if it cant merge
			if (!(stat.getClearStatData() instanceof Mergeable)) { continue; }

			StatHistory hist = StatHistory.from(mmoItem, stat);
			//UPGRD//MMOItems.log(" \u00a73  * \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath());

			// Externals handled elsewhere
			//UPGRD//MMOItems.log(" \u00a7c  *\u00a71*\u00a78* \u00a77GEMS Handled Elsewhere, cleared them \u00a7f" + hist.getAllGemstones().size());
			hist.clearGemstones();

			// Externals handled elsewhere
			//UPGRD//MMOItems.log(" \u00a73  *\u00a79*\u00a79* \u00a77EXSH Handled Elsewhere, cleared them \u00a7f" + hist.getExternalData().size());
			hist.clearExternalData();

			// Get and set
			ret.put(hist.getItemStat(), hist.clone(mmoItem));
			//UPGRD//MMOItems.log(" \u00a79  + \u00a77Storing for Update, History of \u00a7f" + hist.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + hist.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size()+ "\u00a77, Md:\u00a7e" + hist.getAllModifiers().size());
			//UPGRD//hist.log();
		}

		// Yes
		return ret;
	}
	@SuppressWarnings("ConstantConditions")
	void restorePreRNGStats(@NotNull HashMap<? extends ItemStat, ? extends StatHistory> backup, @NotNull MMOItemTemplate template, int determinedItemLevel, boolean cleanModifiers) {

		// Clean older
		if (!cleanModifiers) {
			//UPGRD//MMOItems.log("\u00a73 Clearing RNG Modifiers\u00a77 -->\u00a78-------------------------------------\u00a77-\u00a73-");

			for (ItemStat stat : mmoItem.getStats()) {

				if (mmoItem.getData(stat) instanceof Mergeable) {
					//UPGRD//MMOItems.log("\u00a77   --> \u00a73" + stat.getId() + "\u00a78-------\u00a7f " + mmoItem.getData(stat) + " \u00a78------\u00a77-\u00a73-");

					// Clear modifiers
					StatHistory clear = StatHistory.from(mmoItem, stat); //UPGRD//clear.log();
					clear.clearModifiersBonus();
				}
			} }

		/*
		 * Extra step: Check every stat history
		 */
		//UPGRD//MMOItems.log("\u00a7e Restore PreRNG Stats\u00a77 -->\u00a78-------------------------------------\u00a77-\u00a7e-");
		int l = mmoItem.getUpgradeLevel();
		for (ItemStat stat : backup.keySet()) {
			//UPGRD//MMOItems.log("\u00a7e @\u00a77 " + stat.getId());

			// Get history
			StatHistory hist = backup.get(stat);
			if (hist == null) { continue; }

			// Alr what the template say
			RandomStatData source = template.getBaseItemData().get(stat);

			StatData keptData = shouldRerollRegardless(stat, source, hist.getOriginalData(), determinedItemLevel);
			StatHistory clear = StatHistory.from(mmoItem, stat);
			if (keptData != null) { clear.setOriginalData(keptData); }

			//MOD//MMOItems.log("\u00a77 --> \u00a7cPre-Modified History of this Stat:");
			//MOD//clear.log();

			//MOD//MMOItems.log("\u00a77 --> \u00a7cModifiers History of this Stat:");
			//MOD//hist.log();

			// Clean Modifiers = Forget old ones and generate new ones
			if (cleanModifiers) {
				//MOD//MMOItems.log("\u00a77 --* \u00a7cCleared cached modifiers");
				hist.clearModifiersBonus();

			// Unclean Modifiers - Keep old ones
			} else {
				//MOD//MMOItems.log("\u00a77 --* \u00a76Cleared newer modifiers");

				// Remove any newly-generated modifiers
				clear.clearModifiersBonus();

				// Include old modifiers
				for (UUID gem : hist.getAllModifiers()) {
					//MOD//MMOItems.log("\u00a77 --* \u00a7bTransferred old \u00a78" + gem.toString() + " \u00a7e" + hist.getModifiersBonus(gem));
					clear.registerModifierBonus(gem, hist.getModifiersBonus(gem)); }
			}

			// Store
			StatHistory cachedGemsEX = itemDataHistory.get(stat);
			if (cachedGemsEX != null) { clear.assimilate(cachedGemsEX); }
			itemDataHistory.put(stat, clear);
			mmoItem.setStatHistory(stat, clear);

			//UPGRD//MMOItems.log("\u00a77 --> \u00a7aFinal History of this Stat:");
			//UPGRD//clear.log();
		}
	}
	/**
	 * @return The item is supposedly being updated, but that doesnt mean all its stats must remain the same.
	 *
	 * 		   In contrast to reforging, in which it is expected its RNG to be rerolled, updating should not do it
	 * 		   except in the most dire scenarios:
	 * 		    + The mean/standard deviation changing significantly:
	 * 		    	If the chance of getting the same roll is ridiculously low (3.5SD) under the new settings, reroll.
	 *
	 * 		    + The stat is no longer there, or a new stat was added
	 * 		       The chance of getting a roll of 0 will be evaluated per the rule above.
	 *
	 *
	 */
	@Nullable StatData shouldRerollRegardless(@NotNull ItemStat stat, @NotNull RandomStatData source, @NotNull StatData original, int determinedItemLevel) {

		// Not Mergeable, impossible to keep
		if (!(source instanceof UpdatableRandomStatData)) { return null; }

		/*
		 * These stats are exempt from this 'keeping' operation.
		 * Probably because there is a ReforgeOption specifically
		 * designed for them that keeps them separately
		 */
		if (ItemStats.LORE.equals(stat) ||
			ItemStats.NAME.equals(stat) ||
			ItemStats.GEM_SOCKETS.equals(stat)) {

			return null; }

		// Just pass on
		return ((UpdatableRandomStatData) source).reroll(stat, original, determinedItemLevel);
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

		//UPGRD//MMOItems.log("\u00a79*\u00a77 Regenerating... \u00a7d" + template.getId() + " " + template.getType());

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
			int iLevel = defaultItemLevel;

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

		// Initialize as Volatile, find source template. GemStones require a Live MMOItem though (to correctly load all Stat Histories and sh)
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(miTypeName, miID); ItemMeta meta = nbtItem.getItem().getItemMeta();
		if (template == null) { MMOItems.print(null, "Could not find template for $r{0} {1}$b. ", "MMOItems Reforger", miTypeName.toString(), miID); mmoItem = null; return; }
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));
		if (options.isRegenerate()) { regenerate(player, template); return; }

		// Load live
		loadLiveMMOItem();

		// Keep name
		if (options.shouldKeepName()) { keepName(meta);}

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) { keepLore(options.getKeepCase()); }

		EnchantListData ambiguouslyOriginalEnchantmentCache = null;
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
			cachedName = ((NameData) mmoItem.getData(ItemStats.NAME)).getMainName();

			// No name defined, use display name I guess (pretty unusual btw)
		} else if (meta.hasDisplayName()) {

			cachedName = meta.getDisplayName(); }

		//UPDT//MMOItems.log(" \u00a73  + \u00a77" + cachedName);
	}
	/**
	 * Examines the MMOItems Lore of the item being updated, and chooses which lore
	 * lines are desirable to keep (Those that start with §7)
	 */
	void keepLore(@NotNull String keepCase) {
		if (!mmoItem.hasData(ItemStats.LORE)) { return; }
		cachedLore = extractLore(((StringListData) mmoItem.getData(ItemStats.LORE)).getList(), keepCase);
	}

	@NotNull ArrayList<String> extractLore(@NotNull List<String> lore, @NotNull String keepCase) {

		//UPGRD//MMOItems.log(" \u00a7d> \u00a77Keeping Lore");
		ArrayList<String> ret = new ArrayList<>();

		// Examine every element
		for (String str : lore) {
			//UPGRD//MMOItems.log(" \u00a7d>\u00a7c-\u00a7e- \u00a77Line:\u00a7f " + str);

			// Does it start with the promised...?
			if (str.startsWith(keepCase)) {
				//UPGRD//MMOItems.log(" \u00a72>\u00a7a-\u00a7e- \u00a77Kept");
				ret.add(str); }
		}

		//UPGRD//MMOItems.log(" \u00a7d> \u00a77Result");
		//UPGRD//for (String lr : cachedLore) { MMOItems.log(" \u00a7d  + \u00a77" + lr); }
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
		//UPGRD//MMOItems.log(" \u00a7b> \u00a77Keeping Enchantments");

		// Enchant list data
		cachedEnchantments = new EnchantListData();

		// Does it have MMOItems enchantment data?
		if (!mmoItem.hasData(ItemStats.ENCHANTS)) {
			//UPGRD//MMOItems.log("  \u00a7b* \u00a77No Data, created blanc");
			mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData()); }
		//UPGRD//else { MMOItems.log("  \u00a7b* \u00a77Found Data"); }

		// Make sure they are consolidated
		Enchants.separateEnchantments(mmoItem);

		// Gather
		StatHistory hist = StatHistory.from(mmoItem, ItemStats.ENCHANTS);
		ambiguouslyOriginalEnchantmentCache.setValue((EnchantListData) ((Mergeable) hist.getOriginalData()).cloneData());

		//UPGRD//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Prime Arcane Report: \u00a7b-------------------------");
		//UPGRD//MMOItems.log("  \u00a73> \u00a77History:");
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
		//UPGRD//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
		//UPGRD//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
		//UPGRD//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }
		//UPGRD//MMOItems.log("  \u00a73> \u00a77Cached:");
		//UPGRD//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }
		//UPGRD//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
		//UPGRD//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getValue().getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getValue().getLevel(e)); }

		// Reap
		for (StatData pEnchants : hist.getExternalData()) {

			// It really should be but whatever
			if (pEnchants instanceof EnchantListData) {

				// Merge bruh
				((Mergeable) pEnchants).merge(cachedEnchantments);

				// For every stat
				for (Enchantment e : ((EnchantListData) pEnchants).getEnchants()) {

					// Get result
					int calculated = ((EnchantListData) pEnchants).getLevel(e);

					// Put
					cachedEnchantments.addEnchant(e, calculated);
					//UPGRD//MMOItems.log("  \u00a7b + \u00a77" + e.getName() + " " + calculated);
				}
			}
		}

		// The cache now stores the full extent of extraneous data. Separate from thy history. (As to not include it in this in the cached data later)
		hist.getExternalData().clear();

		//UPGRD//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Arcane Report: \u00a7b-------------------------");
		//UPGRD//MMOItems.log("  \u00a73> \u00a77History:");
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
		//UPGRD//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
		//UPGRD//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
		//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
		//UPGRD//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }
		//UPGRD//MMOItems.log("  \u00a73> \u00a77Cached:");
		//UPGRD//for (Enchantment e : cachedEnchantments.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + cachedEnchantments.getLevel(e)); }
		//UPGRD//MMOItems.log("  \u00a73> \u00a77Ambiguous:");
		//UPGRD//for (Enchantment e : ambiguouslyOriginalEnchantmentCache.getValue().getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ambiguouslyOriginalEnchantmentCache.getValue().getLevel(e)); }
	}
	/**
	 * Just copies the upgrade data, with info on the level of course.
	 */
	void keepUpgrades() {
		//UPGRD//MMOItems.log(" \u00a7e> \u00a77Keeping Upgrade Data");

		// Get Level
		cachedUpgradeLevel = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));
		//UPGRD//MMOItems.log(" \u00a7e>-- \u00a77Level: \u00a7b" + (cachedUpgradeLevel != null ? cachedUpgradeLevel.getLevel() : "0 \u00a78(null)"));
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

			// Cache that gemstone data
			cachedGemStones = (GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS); }

		//UPDT//MMOItems.log(" \u00a7a++ \u00a77Saving current histories (the information on current RNG rolls)");
		// Store all the history of stat proceedings.
		for (StatHistory hist : mmoItem.getStatHistories()) {
			//UPDT//MMOItems.log(" \u00a7c  + \u00a77Original History of \u00a7f" + hist.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + hist.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size());
			StatHistory cloned = hist.clone(mmoItem);

			// Clear externals
			if (clearExternalSH) {
				//UPDT//MMOItems.log(" \u00a7e + \u00a77Clearing \u00a73 " + cloned.getExternalData().size() + " \u00a77EXSH... \u00a7f");
				cloned.clearExternalData(); }

			// Modifiers handled elsewhere
			//UPDT//MMOItems.log(" \u00a7e  + \u00a77Clearing \u00a7c" + cloned.getAllModifiers().size() + " \u00a77Modifiers... \u00a7f");
			cloned.clearModifiersBonus();

			// Get and set
			itemDataHistory.put(cloned.getItemStat(), cloned);
			//UPDT//MMOItems.log(" \u00a7a +++ \u00a77Cloned History of \u00a7f" + cloned.getItemStat().getNBTPath() + "\u00a77, Gx:\u00a7e" + cloned.getAllGemstones().size() + "\u00a77, Ex:\u00a7e" + hist.getExternalData().size());
			//UPDT//cloned.log();
		}
	}
	/**
	 * Just copies the soulbound :B
	 */
	void keepSoulbound() {
		//UPGRD//MMOItems.log(" \u00a7c> \u00a77Keeping Soulbind");

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

			//UPGRD//MMOItems.log(" \u00a7e@ \u00a77Applying Upgrade");

			// If has a upgrade template defined, just remember the level
			if (buildingMMOItem.hasData(ItemStats.UPGRADE)) {
				//UPGRD//MMOItems.log("  \u00a7e* \u00a77Existing Upgrade Detected");

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
			//UPDT//MMOItems.log(" \u00a72 *\u00a76*\u00a7c->>>> \u00a77Cached History (histOLD)");
			//UPDT//histOld.log();

			// Regenerate the original data
			StatHistory hist = StatHistory.from(buildingMMOItem, stat);

			//UPDT//MMOItems.log(" \u00a72 *\u00a76*\u00a7c->>>> \u00a77Current History");
			//UPDT//hist.log();

			// Remember... (but the modifiers are already in there)
			histOld.clearModifiersBonus();
			hist.getExternalData().clear();
			hist.assimilate(histOld);

			// Recalculate
			//HSY//MMOItems.log(" \u00a7b-\u00a7e- \u00a77Reforging Prep Recalculation \u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-\u00a7b-\u00a7e-");
			buildingMMOItem.setData(hist.getItemStat(), hist.recalculate(false, l));
			//UPDT//MMOItems.log(" \u00a72-\u00a76---> \u00a77Final \u00a76<---\u00a72-");
			//UPDT//hist.log();

			//LVL//MMOItems.log(" \u00a7d?\u00a77?\u00a76? \u00a77Lvl: \u00a7b" + buildingMMOItem.getUpgradeLevel());
		}
		//GEM//MMOItems.log(" \u00a7a>1 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>1 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>1 \u00a77Emp: \u00a76" + str); }

		// Apply soulbound
		if (cachedSoulbound != null) {
			//UPGRD//MMOItems.log(" \u00a7c@ \u00a77Applying Soulbind");

			// Apply
			buildingMMOItem.setData(ItemStats.SOULBOUND, cachedSoulbound);
		}

		// Contained enchantments huh
		if (cachedEnchantments != null) {
			//UPGRD//MMOItems.log(" \u00a7b@ \u00a77Applying Enchantments");
			//UPGRD//for (Enchantment lr : cachedEnchantments.getEnchants()) { MMOItems.log(" \u00a7b  + \u00a77" + lr.getName() + " \u00a7f" + cachedEnchantments.getLevel(lr)); }


			// Register as extraneous obviously
			StatHistory hist = StatHistory.from(buildingMMOItem, ItemStats.ENCHANTS);
			hist.registerExternalData(cachedEnchantments.cloneData());

			//UPGRD//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Late Arcane Report: \u00a79-------------------------");
			//UPGRD//MMOItems.log("  \u00a73> \u00a77History:");
			//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
			//UPGRD//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
			//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
			//UPGRD//for (UUID data : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + data.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(data)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(data)).getLevel(e)); } }
			//UPGRD//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
			//UPGRD//for (StatData data : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) data).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) data).getLevel(e)); } }

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

				// Get current ig
				GemSocketsData current = ((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
				//UPDT//MMOItems.log("  \u00a7a* \u00a77Existing Data Detected\u00a7a " + current.toString());

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
				//UPDT//MMOItems.log("  \u00a7a* \u00a77Operation Result\u00a7a " + primeGems.toString());

				// That's the original data
				StatHistory gemStory = StatHistory.from(buildingMMOItem, ItemStats.GEM_SOCKETS);
				gemStory.setOriginalData(primeGems);
				//UPDT//MMOItems.log("  \u00a7a* \u00a77History Final\u00a7a --------");
				//UPDT//gemStory.log();

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
			//UPGRD//MMOItems.log(" \u00a7d@ \u00a77Applying Lore");

			// If it has lore, add I guess
			if (buildingMMOItem.hasData(ItemStats.LORE)) {
				//UPGRD//MMOItems.log("  \u00a7d* \u00a77Inserting first");

				// Get current ig
				StringListData current = ((StringListData) buildingMMOItem.getData(ItemStats.LORE));

				// Get those damn empty sockets
				ArrayList<String> listYes = new ArrayList<>(current.getList());

				// Append to the end of the cached >:]
				cachedLore.addAll(listYes);
			}

			// Create stat
			StringListData sData = new StringListData(cachedLore);
			//UPGRD//for (String lr : cachedLore) { MMOItems.log(" \u00a7d  + \u00a77" + lr); }

			// Set that as the lore
			buildingMMOItem.setData(ItemStats.LORE, sData);
		}

		// Name
		if (cachedName != null) {
			//UPDT//MMOItems.log(" \u00a73@ \u00a77Restoring Name");

			// Find SH
			StatHistory hist = StatHistory.from(buildingMMOItem, ItemStats.NAME);

			// Overwrite whatever is the current name with the cached main name yea
			((NameData) hist.getOriginalData()).setString(cachedName);

			// Recalculate Name
			buildingMMOItem.setData(ItemStats.NAME, hist.recalculate(buildingMMOItem.getUpgradeLevel()));

			//UPDT//hist.log();
		}

		// Apply upgrades
		if (buildingMMOItem.hasUpgradeTemplate()) {
			//UPGRD//MMOItems.log(" \u00a7f@ \u00a77Recalculating Upgrades");

			buildingMMOItem.getUpgradeTemplate().upgradeTo(buildingMMOItem, buildingMMOItem.getUpgradeLevel()); }

		// Apply durability
		if (cachedDurability != null) { mmoItem.setData(ItemStats.DURABILITY, cachedDurability); }

		//GEM//MMOItems.log(" \u00a7a>3 \u00a77Regenerated Gem Sockets:\u00a7f " + buildingMMOItem.getData(ItemStats.GEM_SOCKETS));
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getGemstones()), (s) -> (s instanceof GemstoneData ? ((GemstoneData) s).getHistoricUUID() + "\u00a7f " + ((GemstoneData) s).getName() : "null"))) { MMOItems.log(" \u00a7a+>3 \u00a77Gem: \u00a7a" + str); }
		//GEM//if (buildingMMOItem.getData(ItemStats.GEM_SOCKETS) instanceof GemSocketsData) for (String str : SilentNumbers.transcribeList(new ArrayList<>(((GemSocketsData) buildingMMOItem.getData(ItemStats.GEM_SOCKETS)).getEmptySlots()), (s) -> s + "")) { MMOItems.log(" \u00a7a+>3 \u00a77Emp: \u00a76" + str); }

		// Build and set amount
		ItemStack stack = buildingMMOItem.newBuilder().build();
		stack.setAmount(amount);

		if (cachedDur != null && stack.getItemMeta() instanceof Damageable) { ((Damageable) stack.getItemMeta()).setDamage(SilentNumbers.floor(cachedDur * stack.getType().getMaxDurability())); }

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
