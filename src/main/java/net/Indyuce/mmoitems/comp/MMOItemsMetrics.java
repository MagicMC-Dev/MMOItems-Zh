package net.Indyuce.mmoitems.comp;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.comp.Metrics;

public class MMOItemsMetrics extends Metrics {
	public MMOItemsMetrics() {
		super(MMOItems.plugin);
		addCustomChart(new Metrics.SingleLineChart("items", () -> {
			int total = 0;
			for (Type type : MMOItems.plugin.getTypes().getAll())
				total += type.getConfigFile().getConfig().getKeys(false).size();
			return total;
		}));
	}
}
