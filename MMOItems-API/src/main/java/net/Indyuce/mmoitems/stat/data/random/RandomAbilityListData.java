package net.Indyuce.mmoitems.stat.data.random;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomAbilityListData implements RandomStatData<AbilityListData> {
	private final Set<RandomAbilityData> abilities = new LinkedHashSet<>();

	public RandomAbilityListData(RandomAbilityData... abilities) {
		add(abilities);
	}

	public void add(RandomAbilityData... abilities) {
		this.abilities.addAll(Arrays.asList(abilities));
	}

	public Set<RandomAbilityData> getAbilities() {
		return abilities;
	}

	@Override
	public AbilityListData randomize(MMOItemBuilder builder) {
		AbilityListData list = new AbilityListData();
		abilities.forEach(random -> list.add(random.randomize(builder)));
		return list;
	}
}
