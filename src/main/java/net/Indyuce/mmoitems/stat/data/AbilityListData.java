package net.Indyuce.mmoitems.stat.data;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class AbilityListData implements StatData, Mergeable {
	private final Set<AbilityData> abilities = new LinkedHashSet<>();

	public AbilityListData(AbilityData... abilities) {
		add(abilities);
	}

	public void add(AbilityData... abilities) {
		this.abilities.addAll(Arrays.asList(abilities));
	}

	public Set<AbilityData> getAbilities() {
		return abilities;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof AbilityListData, "Cannot merge two different stat data types");
		abilities.addAll(((AbilityListData) data).abilities);
	}
}