package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.util.message.FriendlyFeedbackPalette_MMOItems;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.DoubleData;
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
		Validate.notNull(config, FriendlyFeedbackProvider.QuickForConsole(FriendlyFeedbackPalette_MMOItems.get(), "You must specify a config section."));

		// Build ID
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		// Feedback
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FriendlyFeedbackPalette_MMOItems.get());
		ffp.ActivatePrefix(true, "Upgrade Template $i&o" + config.getName());

		// For ever stat
		for (String key : config.getKeys(false)) {
			//UPGRD//MMOItems. Log("\u00a7a>>> \u00a77Stat \u00a72" + key);

			// Get internal stat ID
			String statFormat = key.toUpperCase().replace("-", "_");

			// Attempt to find stat
			ItemStat stat = MMOItems.plugin.getStats().get(statFormat);
			if (stat == null) { ffp.Log(FriendlyFeedbackCategory.ERROR, "Stat '$r{0}$b' $fnot found$b.", statFormat); continue; }
			if (!(stat instanceof Upgradable)) { ffp.Log(FriendlyFeedbackCategory.ERROR, "Stat $r{0}$b is $fnot upgradeable$b.", stat.getId()); continue; }
			if (!(stat.getClearStatData() instanceof Mergeable)) { ffp.Log(FriendlyFeedbackCategory.ERROR, "Stat Data used by $r{0}$b is $fnot mergeable$b, and thus it cannot be upgradeable. Contact the dev of this ItemStat.", stat.getId()); continue; }

			// Attempt to parse Upgrade Info
			try {

				// Parsed correctly? Add
				perStatUpgradeInfos.put(stat, ((Upgradable) stat).loadUpgradeInfo(config.get(key)));

			// Somethings up, generate exception ig
			} catch (IllegalArgumentException exception) {

				// Log
				ffp.Log(FriendlyFeedbackCategory.ERROR, exception.getMessage());
			}
		}

		// Print all failures
		ffp.SendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
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

		// Set the items level
		UpgradeData dat;
		if (mmoitem.hasData(ItemStats.UPGRADE)) { dat = (UpgradeData) mmoitem.getData(ItemStats.UPGRADE); } else { dat = new UpgradeData(null, null, false, false, 0, 100); }
		dat.setLevel(level);
		mmoitem.setData(ItemStats.UPGRADE, dat);

		// For every Stat-UpgradeInfo pair
		for (ItemStat stat : perStatUpgradeInfos.keySet()) {

			// Preprocess
			((Upgradable) stat).preprocess(mmoitem);

			// Initializes Stat History
			StatHistory<StatData> hist = StatHistory.From(mmoitem, stat);

			// Midprocess
			((Upgradable) stat).midprocess(mmoitem);

			// The Stat History now manages applying upgrades.
			mmoitem.setData(stat, hist.Recalculate());

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
