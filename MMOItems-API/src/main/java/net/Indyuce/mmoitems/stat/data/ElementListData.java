package net.Indyuce.mmoitems.stat.data;

import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.util.ElementStatType;
import net.Indyuce.mmoitems.util.Pair;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ElementListData implements Mergeable {
    private final Map<Pair<Element, ElementStatType>, Double> stats = new LinkedHashMap<>();

    public double getStat(Element element, ElementStatType statType) {
        Double found = stats.get(Pair.of(element, statType));
        return found == null ? 0 : found;
    }

    public Set<Pair<Element, ElementStatType>> getKeys() {
        return stats.keySet();
    }

    public void setStat(Element element, ElementStatType statType, double value) {
        stats.put(Pair.of(element, statType), value);
    }

    @Override
    public void merge(StatData data) {
        Validate.isTrue(data instanceof ElementListData, "Cannot merge two different stat data types");
        ElementListData extra = (ElementListData) data;
        //Includes old values if any, fixes stacking of element double values I believe - Kilo
        extra.stats.forEach((key, value) -> stats.put(key, value + stats.getOrDefault(key,0.0)));
    }

    @NotNull
    @Override
    public StatData cloneData() {
        ElementListData ret = new ElementListData();
        for (Map.Entry<Pair<Element, ElementStatType>, Double> entry : stats.entrySet()) {
            Pair<Element, ElementStatType> key = entry.getKey();
            Double value = entry.getValue();
            if (value != 0) {
                ret.stats.put(key, value);
            }
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        return stats.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementListData that = (ElementListData) o;
        return stats.equals(that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stats);
    }
}