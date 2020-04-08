package net.Indyuce.mmoitems.api.itemgen.loot.restriction;

import java.util.function.Predicate;

import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ClassFilter implements Predicate<GenerationTemplate> {
	private final String name;

	public ClassFilter(RPGPlayer player) {
		this(player.getClassName());
	}

	public ClassFilter(String name) {
		this.name = name;
	}

	@Override
	public boolean test(GenerationTemplate template) {
		if (!template.getBaseItemData().containsKey(ItemStat.REQUIRED_CLASS))
			return true;

		// mandatory equalsIgnoreCase
		for (String profess : ((StringListData) template.getBaseItemData().get(ItemStat.REQUIRED_CLASS)).getList())
			if (profess.equalsIgnoreCase(name))
				return true;

		return false;
	}
}
