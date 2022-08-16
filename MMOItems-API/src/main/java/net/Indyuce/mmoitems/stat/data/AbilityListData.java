package net.Indyuce.mmoitems.stat.data;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class AbilityListData implements StatData, Mergeable<AbilityListData> {
	@NotNull private final Set<AbilityData> abilities = new LinkedHashSet<>();

	public AbilityListData(@NotNull AbilityData... abilities) {
		add(abilities);
	}

	public AbilityListData(@NotNull Set<AbilityData> abilit) { add(abilit); }

	public void add(@NotNull AbilityData... abilities) {
		this.abilities.addAll(Arrays.asList(abilities));
	}

	public void add(@NotNull Set<AbilityData> abilit) { abilities.addAll(abilit); }

	@NotNull
	public Set<AbilityData> getAbilities() {
		return abilities;
	}

	@Override
	public void merge(@NotNull AbilityListData data) {
		abilities.addAll(data.abilities);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbilityListData)) { return false; }

		// Different number of abilities? Not equal
		if (getAbilities().size() != ((AbilityListData) obj).getAbilities().size()) { return false; }

		// Examine each
		for (AbilityData ab : ((AbilityListData) obj).getAbilities()) {

			if (ab == null) { continue; }

			boolean unmatched = true;
			for (AbilityData thi : getAbilities()) { if (ab.equals(thi)) { unmatched = false; break; } }

			// Extraneous ability found, not equal.
			if (unmatched) { return false; }
		}

		return true; }

	@Override
	public @NotNull AbilityListData cloneData() { return new AbilityListData(getAbilities()); }

	@Override
	public boolean isEmpty() {
		return abilities.isEmpty();
	}
}