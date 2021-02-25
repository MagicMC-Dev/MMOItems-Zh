package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.FriendlyFeedbackPalette_MMOItems;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.GemSockets;
import net.Indyuce.mmoitems.stat.RevisionID;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.apache.commons.lang.Validate;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	// region Item Data

	// Raw NBT Item
	@NotNull private final NBTItem nbtItem;

	// ItemStack size
	private final int amount;

	// Not initialized at first for performance reasons
	private MMOItem mmoItem;

	// Data
	private final Map<ItemStat, StatData> itemData = new HashMap<>();
	private final Map<ItemStat, StatHistory<StatData>> itemDataHistory = new HashMap<>();

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
		applySoulbound(p, MMOItems.plugin.getConfig().getInt("soulbound.auto-bind.level", 1));
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

		/*
		 *  todo Has to store every stat into itemData, then check each stat of
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

		reforge(player, options);
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
		if (!options.shouldKeepGemStones()) { loadVolatileMMOItem(); } else { loadLiveMMOItem(); }
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoItem.getType(), mmoItem.getId());
		ItemMeta meta = nbtItem.getItem().getItemMeta();
		Validate.isTrue(meta != null, FriendlyFeedbackProvider.QuickForConsole(FriendlyFeedbackPalette_MMOItems.get(), "Invalid item meta prevented $f{0}$b from updating.", template.getType().toString() + " " + template.getId()));

		// Keep name
		if (options.shouldKeepName()) {
			//UPDT//MMOItems.Log(" \u00a73> \u00a77Keeping Name");

			// Does it have a name?
			if (mmoItem.hasData(ItemStats.NAME)) {

				// Cache it
				cachedName = mmoItem.getData(ItemStats.NAME).toString();

			// No name defined, use display name I guess (pretty unusual btw)
			} else if (meta.hasDisplayName()) {

				cachedName = meta.getDisplayName();
			}

			//UPDT//MMOItems.Log(" \u00a73  + \u00a77" + cachedName);
		}

		// Keep specific lore components
		if (options.shouldKeepLore() && mmoItem.hasData(ItemStats.LORE)) {
			//UPDT//MMOItems.Log(" \u00a7d> \u00a77Keeping Lore");

			// Examine every element
			for (String str : ((StringListData) mmoItem.getData(ItemStats.LORE)).getList()) {

				// Does it start with the promised...?
				if (str.startsWith("\u00a77")) { cachedLore.add(str); }
			}

			//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.Log(" \u00a7d  + \u00a77" + lr); }
		}

		// Choose enchantments to keep
		if (options.shouldKeepEnchantments()) {
			//UPDT//MMOItems.Log(" \u00a7b> \u00a77Keeping Enchantments");

			// Enchant list data
			cachedEnchantments = new EnchantListData();

			// Does it have MMOItems enchantment data?
			if (mmoItem.hasData(ItemStats.ENCHANTS)) {
				//UPDT//MMOItems.Log("  \u00a7b* \u00a77Found Data");

			// Nope
			} else {
				//UPDT//MMOItems.Log("  \u00a7b* \u00a77No Data");
				mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());
			}

			// Make sure they are consolidated
			Enchants.separateEnchantments(mmoItem);

			// Gather
			StatHistory<StatData> hist = StatHistory.From(mmoItem, ItemStats.ENCHANTS);

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
						//UPDT//MMOItems.Log("  \u00a7b + \u00a77" + e.getName() + " " + calculated);
					}
				}
			}

			// The cache now stores the full extent of extraneous data. Separate from thy history. (As to not include it in this in the cached data later)
			hist.getExternalData().clear();
		}

		// Acquire old upgrade level
		if (options.shouldKeepUpgrades() && mmoItem.hasData(ItemStats.UPGRADE)) {
			//UPDT//MMOItems.Log(" \u00a7e> \u00a77Keeping Upgrade Data");

			// Get Level
			cachedUpgradeLevel = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));
		}

		// Gather Gemstones
		if (options.shouldKeepGemStones() && mmoItem.hasData(ItemStats.GEM_SOCKETS)) {
			//UPDT//MMOItems.Log(" \u00a7a> \u00a77Keeping Gem Sockets");

			// Cache that gemstone data
			cachedGemStones = (GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS);

			// Store all the history of stat proceedings.
			for (StatHistory<StatData> hist : mmoItem.getStatHistories()) {
				//UPDT//MMOItems.Log(" \u00a7a  + \u00a77History of \u00a7f" + hist.getItemStat().getNBTPath());

				// Get and set
				itemDataHistory.put(hist.getItemStat(), hist);
			}
		}

		// Soulbound transfer
		if (options.shouldKeepSoulbind() && mmoItem.hasData(ItemStats.SOULBOUND)) {
			//UPDT//MMOItems.Log(" \u00a7c> \u00a77Keeping Soulbind");

			// Find data
			cachedSoulbound = mmoItem.getData(ItemStats.SOULBOUND);
		}

		if (player == null) {

			// Get default Item Level
			final int iLevel = MMOItems.plugin.getConfig().getInt("item-revision.default-item-level", -32767);

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
					(mmoItem.hasData(ItemStats.TIER) && MMOItems.plugin.getConfig().getBoolean("item-revision.keep-tiers")) ?

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
	}

	/**
	 * Was any reforge actually performed on this item?
	 */
	public boolean hasChanges() { return mmoItem != null; }

	public ItemStack toStack() {

		// For every cached stat (presumably due to its importance)
		for (ItemStat stat : itemData.keySet()) {
			//UPDT//MMOItems.Log(" \u00a72@\u00a78@ \u00a77Cached Stat");

			// Replace into incoming MMOItem
			StatData data = itemData.get(stat);

			// Include if nonull
			if (data != null) {
				//UPDT//MMOItems.Log(" \u00a72@\u00a78@ \u00a77Old stat \u00a7f" + stat.getNBTPath());

				// Set, replace, begone the previous!
				mmoItem.setData(stat, data);
			}
		}

		// Apply histories
		for (ItemStat stat : itemDataHistory.keySet()) {
			//UPDT//MMOItems.Log(" \u00a72@\u00a76@ \u00a77Cached Stat History");

			// Does it have history too?
			StatHistory<StatData> histOld = itemDataHistory.get(stat);
			if (histOld != null) {
				//UPDT//MMOItems.Log(" \u00a72 *\u00a76* \u00a77Of old stat \u00a7f" + histOld.getItemStat().getNBTPath());

				// Regenerate the original data
				StatHistory<StatData> hist = StatHistory.From(mmoItem, stat);

				// Remember...
				hist.Assimilate(histOld);

				// Recalculate
				mmoItem.setData(hist.getItemStat(), hist.Recalculate(false));
			}
		}

		// Apply soulbound
		if (cachedSoulbound != null) {
			//UPDT//MMOItems.Log(" \u00a7c@ \u00a77Applying Soulbind");

			// Apply
			mmoItem.setData(ItemStats.SOULBOUND, cachedSoulbound);
		}

		// Contained enchantments huh
		if (cachedEnchantments != null) {
			//UPDT//MMOItems.Log(" \u00a7b@ \u00a77Applying Enchantments");

			// Register as extraneous obviously
			StatHistory<StatData> hist = StatHistory.From(mmoItem, ItemStats.ENCHANTS);
			hist.registerExternalData(cachedEnchantments);
			//UPDT//for (Enchantment lr : cachedEnchantments.getEnchants()) { //UPDT//MMOItems. Log(" \u00a7b  + \u00a77" + lr.getName() + " \u00a7f" + cachedEnchantments.getLevel(lr)); }
		}

		// Upgrade Information
		if (cachedUpgradeLevel != null) {
			//UPDT//MMOItems.Log(" \u00a7e@ \u00a77Applying Upgrade");

			// If has a upgrade template defined, just remember the level
			if (mmoItem.hasData(ItemStats.UPGRADE)) {
				//UPDT//MMOItems.Log("  \u00a7e* \u00a77Existing Upgrade Detected");

				// Get current ig
				UpgradeData current = ((UpgradeData) mmoItem.getData(ItemStats.UPGRADE));

				// Edit level
				current.setLevel(Math.min(cachedUpgradeLevel.getLevel(), current.getMaxUpgrades()));
				//UPDT//MMOItems.Log("  \u00a7e + \u00a77Set to level \u00a7f" + current.getLevel());

				// Re-set cuz why not
				mmoItem.setData(ItemStats.UPGRADE, current);

			// Otherwise, the level AND template shall prevail
			} else {
				//UPDT//MMOItems.Log("  \u00a7e* \u00a77Using Cached");

				// Set from the cached
				mmoItem.setData(ItemStats.UPGRADE, cachedUpgradeLevel);
				//UPDT//MMOItems.Log("  \u00a7e + \u00a77Set to level \u00a7f" + cachedUpgradeLevel.getLevel());
			}
		}

		// Gem Stones
		if (cachedGemStones != null) {
			//UPDT//MMOItems.Log(" \u00a7a@ \u00a77Applying Gemstones");

			// If has a upgrade template defined, just remember the level
			if (mmoItem.hasData(ItemStats.GEM_SOCKETS)) {
				//UPDT//MMOItems.Log("  \u00a7a* \u00a77Existing Data Detected");

				// Get current ig
				GemSocketsData current = ((GemSocketsData) mmoItem.getData(ItemStats.GEM_SOCKETS));

				// Get those damn empty sockets
				ArrayList<String> availableSockets = new ArrayList<>(current.getEmptySlots());
				ArrayList<GemstoneData> oldSockets = new ArrayList<>(cachedGemStones.getGemstones());

				// Remaining
				for (GemstoneData data : oldSockets) {
					//UPDT//MMOItems.Log("  \u00a7a*\u00a7e* \u00a77Fitting \u00a7f" + data.getHistoricUUID().toString());

					// No more if no more sockets left
					if (availableSockets.size() <= 0) {
						//UPDT//MMOItems.Log(" \u00a7a  +\u00a7c+ \u00a77No More Sockets");

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
						//UPDT//MMOItems.Log(" \u00a7a  + \u00a77Fit into color \u00a7f" + remembrance);
					}
				}

				// Update list of empty sockets
				cachedGemStones.getEmptySlots().clear();
				cachedGemStones.getEmptySlots().addAll(availableSockets);
			}

			// Set the data, as changed as it may be
			mmoItem.setData(ItemStats.GEM_SOCKETS, cachedGemStones);
		}

		// Lore
		if (!cachedLore.isEmpty()) {
			//UPDT//MMOItems.Log(" \u00a7d@ \u00a77Applying Lore");

			// If it has lore, add I guess
			if (mmoItem.hasData(ItemStats.LORE)) {
				//UPDT//MMOItems.Log("  \u00a7d* \u00a77Inserting first");

				// Get current ig
				StringListData current = ((StringListData) mmoItem.getData(ItemStats.LORE));

				// Get those damn empty sockets
				ArrayList<String> listYes = new ArrayList<>(current.getList());

				// Append to the end of the cached >:]
				cachedLore.addAll(listYes);
			}

			// Create stat
			StringListData sData = new StringListData(cachedLore);
			//UPDT//for (String lr : cachedLore) { //UPDT//MMOItems.Log(" \u00a7d  + \u00a77" + lr); }

			// Set that as the lore
			mmoItem.setData(ItemStats.LORE, sData);
		}

		// Name
		if (cachedName != null) {
			//UPDT//MMOItems.Log(" \u00a73@ \u00a77Applying Name \u00a7f" + cachedName);

			// Replace name completely
			mmoItem.setData(ItemStats.NAME, new StringData(cachedName));
		}

		// Apply upgrades
		if (mmoItem.hasUpgradeTemplate()) { mmoItem.getUpgradeTemplate().upgradeTo(mmoItem, mmoItem.getUpgradeLevel()); }

		// Build and set amount
		ItemStack stack = mmoItem.newBuilder().build();
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
