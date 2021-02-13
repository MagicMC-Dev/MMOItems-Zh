package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Upgradable;

public class UpgradeTemplate {
	private final String id;
	private final Map<ItemStat, UpgradeInfo> stats = new HashMap<>();

	public UpgradeTemplate(ConfigurationSection config) {
		Validate.notNull(config, "You must specify a config section.");

		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		for (String key : config.getKeys(false)) {
			String statFormat = key.toUpperCase().replace("-", "_");

			ItemStat stat = MMOItems.plugin.getStats().get(statFormat);
			Validate.notNull(stat, "Could not read stat ID " + statFormat);
			Validate.isTrue(stat instanceof Upgradable, "Stat " + stat.getId() + " us not upgradable.");

			try {
				stats.put(stat, ((Upgradable) stat).loadUpgradeInfo(config.get(key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"An error occured while trying to load stat '" + key + "' from upgrade template '" + id + "': " + exception.getMessage());
			}
		}
	}

	public String getId() {
		return id;
	}

	public Set<ItemStat> getKeys() {
		return stats.keySet();
	}

	public UpgradeInfo getUpgradeInfo(ItemStat stat) {
		return stats.get(stat);
	}

	public void upgrade(MMOItem mmoitem) {
		for (ItemStat stat : stats.keySet()) {

			// If it has the data to begin with?
			if (mmoitem.hasData(stat)) {

				// Initializes original stats.
				StatHistory.From(mmoitem, stat);

				// Applies changes
				((Upgradable) stat).apply(mmoitem, stats.get(stat));
			}
		}
	}
}
