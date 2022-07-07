package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ElementListData implements Mergeable {
    @NotNull
    private final Map<Element, Double> damage = new HashMap<>(), defense = new HashMap<>();

    /**
     * No way to add the elements directly from the constructor.
     * <p></p>
     * Use the set methods for that.
     */
    public ElementListData() {
    }

    public double getDefense(Element element) {
        return defense.getOrDefault(element, 0d);
    }

    public double getDamage(Element element) {
        return damage.getOrDefault(element, 0d);
    }

    public Set<Element> getDefenseElements() {
        return defense.keySet();
    }

    public Set<Element> getDamageElements() {
        return damage.keySet();
    }

    public void setDamage(Element element, double value) {
        if (value == 0)
            damage.remove(element);
        else
            damage.put(element, value);
    }

    public void setDefense(Element element, double value) {
        if (value == 0)
            defense.remove(element);
        else
            defense.put(element, value);
    }

    /**
     * @return Total amount of stats (damage or defense) registered in that stat data.
     */
    public int total() {
        return damage.size() + defense.size();
    }

    @Override
    public void merge(StatData data) {
        Validate.isTrue(data instanceof ElementListData, "Cannot merge two different stat data types");
        ElementListData extra = (ElementListData) data;

        for (Element element : extra.damage.keySet())
            damage.put(element, extra.damage.get(element) + damage.getOrDefault(element, 0d));
        for (Element element : extra.defense.keySet())
            defense.put(element, extra.defense.get(element) + defense.getOrDefault(element, 0d));
    }

    @NotNull
    @Override
    public StatData cloneData() {
        ElementListData ret = new ElementListData();
        damage.forEach((el, val) -> ret.damage.put(el, val));
        defense.forEach((el, val) -> ret.defense.put(el, val));
        return ret;
    }

    @Override
    public boolean isEmpty() {
        return damage.isEmpty() && defense.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ElementListData)) {
            return false;
        }
        if (((ElementListData) obj).damage.size() != damage.size() || ((ElementListData) obj).defense.size() != defense.size()) {
            return false;
        }

        for (Element e : Element.values()) {

            double expectedDA = getDamage(e);
            double expectedDE = getDefense(e);
            double realDA = ((ElementListData) obj).getDamage(e);
            double realDE = ((ElementListData) obj).getDefense(e);

            // Any differene?
            if (expectedDA != realDA || expectedDE != realDE) {
                return false;
            }
        }

        return true;
    }
}