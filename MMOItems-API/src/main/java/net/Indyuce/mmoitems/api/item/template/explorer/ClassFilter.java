package net.Indyuce.mmoitems.api.item.template.explorer;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.StringListData;

import java.util.function.Predicate;

/**
 * Filters items with a specific class
 */
public class ClassFilter implements Predicate<MMOItemTemplate> {
	private final String name;

	public ClassFilter(RPGPlayer player) {
		this(player.getClassName());
	}

	public ClassFilter(String name) {
		this.name = name;
	}

	@Override
	public boolean test(MMOItemTemplate template) {
		if (!template.getBaseItemData().containsKey(ItemStats.REQUIRED_CLASS))
			return true;

		// mandatory equalsIgnoreCase
		for (String profess : ((StringListData) template.getBaseItemData().get(ItemStats.REQUIRED_CLASS)).getList())
			if (profess.equalsIgnoreCase(name))
				return true;

		return false;
	}
}
