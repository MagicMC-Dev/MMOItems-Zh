package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.Upgrade_Stat.UpgradeData;
import net.Indyuce.mmoitems.stat.data.upgrade.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Upgradable;

public class UpgradeTemplate {
	private final String id;
	private Map<ItemStat, UpgradeInfo> stats = new HashMap<>();

	public UpgradeTemplate(ConfigurationSection config) {
		Validate.notNull(config, "You must specify a config section.");

		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		for (String key : config.getKeys(false)) {
			String statFormat = key.toUpperCase().replace("-", "_");

			ItemStat stat = MMOItems.plugin.getStats().get(statFormat);
			Validate.notNull(stat, "Could not read stat ID " + statFormat);

			if (!(stat instanceof Upgradable)) {
				log("Stat " + stat.getId() + " is not upgradable.");
				continue;
			}

			try {
				stats.put(stat, ((Upgradable) stat).loadUpgradeInfo(config.get(key)));
			} catch (IllegalArgumentException exception) {
				log("Could not load stat " + stat.getId() + ": " + exception.getMessage());
			}
		}
	}

	public void log(String... message) {
		for (String line : message)
			MMOItems.plugin.getLogger().log(Level.WARNING, "[Upgrade template] " + id + ": " + line);
	}

	public String getId() {
		return id;
	}

	public Set<ItemStat> getKeys() {
		return stats.keySet();
	}

	public UpgradeInfo getUpgradeInfo() {
		return stats.get(stats);
	}

	public void upgrade(MMOItem mmoitem, UpgradeData upgrade) {
		for (ItemStat stat : stats.keySet())
			// if (mmoitem.hasData(stat))
			((Upgradable) stat).apply(mmoitem, stats.get(stat));
	}
}
