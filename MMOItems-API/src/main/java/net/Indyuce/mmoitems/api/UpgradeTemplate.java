package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpgradeTemplate {
	@NotNull
	private final String id;
	@NotNull
	private final Map<ItemStat, UpgradeInfo> perStatUpgradeInfos = new HashMap<>();

	/**
	 *  Loads an Upgrade Template directly from the YML file. Neat!
	 */
	public UpgradeTemplate(@NotNull ConfigurationSection config) {
		Validate.notNull(config, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "You must specify a config section."));

		// Build ID
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		// Feedback
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Upgrade Template $i&o" + config.getName());

		// For ever stat
		for (String key : config.getKeys(false)) {
			//UPGRD//MMOItems. Log("\u00a7a>>> \u00a77Stat \u00a72" + key);

			// Get internal stat ID
			String statFormat = key.toUpperCase().replace("-", "_");

			// Attempt to find stat
			ItemStat stat = MMOItems.plugin.getStats().get(statFormat);
			if (stat == null) { ffp.log(FriendlyFeedbackCategory.ERROR, "Stat '$r{0}$b' $fnot found$b.", statFormat); continue; }
			if (!(stat instanceof Upgradable)) { ffp.log(FriendlyFeedbackCategory.ERROR, "Stat $r{0}$b is $fnot upgradeable$b.", stat.getId()); continue; }
			if (!(stat.getClearStatData() instanceof Mergeable)) { ffp.log(FriendlyFeedbackCategory.ERROR, "Stat Data used by $r{0}$b is $fnot mergeable$b, and thus it cannot be upgradeable. Contact the dev of this ItemStat.", stat.getId()); continue; }

			// Attempt to parse Upgrade Info
			try {

				// Parsed correctly? Add
				perStatUpgradeInfos.put(stat, ((Upgradable) stat).loadUpgradeInfo(config.get(key)));

			// Somethings up, generate exception ig
			} catch (IllegalArgumentException exception) {

				// Log
				ffp.log(FriendlyFeedbackCategory.ERROR, exception.getMessage());
			}
		}

		// Print all failures
		ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
	}

	/**
	 * Get the internal ID of this template.
	 * <p></p>
	 * In the format: <code><b>upgrade-template-name</b></code>
	 * <p>(No spaces nor underscores, lowercase)</p>
	 */
	@NotNull public String getId() {
		return id;
	}

	/**
	 * Get the <code>ItemStat</code>s that this template has <code>UpgradeInfo</code> about.
	 */
	@NotNull public Set<ItemStat> getKeys() {
		return perStatUpgradeInfos.keySet();
	}

	/**
	 * Get the <code>UpgradeInfo</code> associated with this stat.
	 */
	@Nullable public UpgradeInfo getUpgradeInfo(@NotNull ItemStat stat) {
		return perStatUpgradeInfos.get(stat);
	}

	/**
	 * Upgrades this MMOItem by 1 level
	 */
	public void upgrade(@NotNull MMOItem mmoitem) {

		// Yes
		upgradeTo(mmoitem, mmoitem.getUpgradeLevel() + 1);
	}

	/**
	 * Upgrades this MMOItem's stats and sets the level.
	 * @param level Target level, which may even be negative!
	 */
	public void upgradeTo(@NotNull MMOItem mmoitem, int level) {

		// Make sure to not overwrite player's enchantments when upgrading.
		Enchants.separateEnchantments(mmoitem);
		//UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Separated enchantments");

		// Set the items level
		UpgradeData dat;
		if (mmoitem.hasData(ItemStats.UPGRADE)) {
			dat = (UpgradeData) mmoitem.getData(ItemStats.UPGRADE);
		} else { dat = new UpgradeData(null, null, false, false, 0, 0, 100); }
		dat.setLevel(level);
		mmoitem.setData(ItemStats.UPGRADE, dat);
		//UPGR//MMOItems.log("\u00a76>\u00a73>\u00a7a> \u00a77Upgrading to level \u00a7e" + dat.getLevel());

		// For every Stat-UpgradeInfo pair
		for (ItemStat stat : perStatUpgradeInfos.keySet()) {
			//UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Upgrading stat \u00a7e" + stat.getId());

			// Preprocess
			((Upgradable) stat).preprocess(mmoitem);

			// Initializes Stat History
			StatHistory hist = StatHistory.from(mmoitem, stat);
			//UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Stat History Initialized");

			// Midprocess
			((Upgradable) stat).midprocess(mmoitem);

			// The Stat History now manages applying upgrades.
			//HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Upgrade Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
			mmoitem.setData(stat, hist.recalculate(level));
			//UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Recalculated");

			// Postprocess
			((Upgradable) stat).postprocess(mmoitem);
		}
	}

	/**
	 * @return If the user has set in the config that the stats should display how the item's upgrades have affected them.
	 */
	public static boolean isDisplayingUpgrades() { return MMOItems.plugin.getConfig().getBoolean("item-upgrading.display-stat-changes", false); }

	/**
	 * The user may define how to display stat changes due to upgrades,
	 * as well as 'negative' and 'positive' colours.
	 *
	 * @return A string ready to just have its colors parsed and inserted into lore.
	 * @param value The <code>toString()</code> of this will replace all instances of <code>#stat#</code> the user specifies in the config.
	 * @param isNegative Should 'negative' coloration be used instead of positive? The user uses the placeholder <code><b>< p ></b></code> in this place.
	 */
	@NotNull public static String getUpgradeChangeSuffix(@NotNull String value, boolean isNegative) {

		// Get the base
		String base = Objects.requireNonNull(MMOItems.plugin.getConfig().getString("item-upgrading.stat-change-suffix", " &8(<p>#stat#&8)"));
		String succ = Objects.requireNonNull(MMOItems.plugin.getConfig().getString("item-upgrading.stat-change-positive", "&a"));
		String fauc = Objects.requireNonNull(MMOItems.plugin.getConfig().getString("item-upgrading.stat-change-negative", "&c"));

		// Parse ig
		if (isNegative) {

			// Failure-colored
			return base.replace("<p>", fauc).replace("#stat#", value);

		// Its a positive upgrade-yo
		} else {

			// Success-coloreds
			return base.replace("<p>", succ).replace("#stat#", value);
		}
	}
}
