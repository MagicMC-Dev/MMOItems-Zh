package net.Indyuce.mmoitems.stat.data;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class AbilityListData implements StatData, Mergeable {
	@NotNull private final Set<AbilityData> abilities = new LinkedHashSet<>();

	public AbilityListData(@NotNull AbilityData... abilities) {
		add(abilities);
	}
	public AbilityListData(@NotNull Set<AbilityData> abilit) { add(abilit); }

	public void add(@NotNull AbilityData... abilities) {
		this.abilities.addAll(Arrays.asList(abilities));
	}
	public void add(@NotNull Set<AbilityData> abilit) { abilities.addAll(abilit); }

	@NotNull public Set<AbilityData> getAbilities() {
		return abilities;
	}

	@Override
	public void merge(@NotNull StatData data) {
		Validate.isTrue(data instanceof AbilityListData, "Cannot merge two different stat data types");
		abilities.addAll(((AbilityListData) data).abilities);
	}

	@Override
	public @NotNull StatData cloneData() { return new AbilityListData(getAbilities()); }
}