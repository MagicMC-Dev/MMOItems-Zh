package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AbilityListData implements StatData, Mergeable<AbilityListData> {
    private final List<AbilityData> abilities = new ArrayList<>();

    public AbilityListData(@NotNull AbilityData... abilities) {
        add(abilities);
    }

    public AbilityListData(@NotNull Collection<AbilityData> abilities) {
        add(abilities);
    }

    public void add(@NotNull AbilityData... abilities) {
        this.abilities.addAll(Arrays.asList(abilities));
    }

    public void add(@NotNull Collection<AbilityData> abilit) {
        abilities.addAll(abilit);
    }

    @NotNull
    public Set<AbilityData> getAbilities() {
        return new HashSet<>(abilities);
    }

    @Override
    public void mergeWith(@NotNull AbilityListData targetData) {
        abilities.addAll(targetData.abilities);
    }

    @NotNull
    @Override
    public AbilityListData clone() {
        return new AbilityListData();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbilityListData)) {
            return false;
        }

        // Different number of abilities? Not equal
        if (getAbilities().size() != ((AbilityListData) obj).getAbilities().size()) {
            return false;
        }

        // Examine each
        for (AbilityData ab : ((AbilityListData) obj).getAbilities()) {

            if (ab == null) {
                continue;
            }

            boolean unmatched = true;
            for (AbilityData thi : getAbilities()) {
                if (ab.equals(thi)) {
                    unmatched = false;
                    break;
                }
            }

            // Extraneous ability found, not equal.
            if (unmatched) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return abilities.isEmpty();
    }
}