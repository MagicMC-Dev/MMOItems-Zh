package net.Indyuce.mmoitems.stat.data.random;

import java.util.LinkedHashSet;
import java.util.Set;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomAbilityListData implements RandomStatData {
	private final Set<RandomAbilityData> abilities = new LinkedHashSet<>();

	public RandomAbilityListData(RandomAbilityData... abilities) {
		add(abilities);
	}

	public void add(RandomAbilityData... abilities) {
		for (RandomAbilityData ability : abilities)
			this.abilities.add(ability);
	}

	public Set<RandomAbilityData> getAbilities() {
		return abilities;
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		AbilityListData list = new AbilityListData();
		abilities.forEach(random -> list.add(random.randomize(builder)));
		return list;
	}
}
