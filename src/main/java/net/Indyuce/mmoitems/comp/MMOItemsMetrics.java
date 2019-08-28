package net.Indyuce.mmoitems.comp;

import java.util.concurrent.Callable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class MMOItemsMetrics extends Metrics {
	public MMOItemsMetrics() {
		super(MMOItems.plugin);
		addCustomChart(new Metrics.SingleLineChart("items", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int total = 0;
				for (Type type : MMOItems.plugin.getTypes().getAll())
					total += type.getConfigFile().getConfig().getKeys(false).size();
				return total;
			}
		}));
	}
}
