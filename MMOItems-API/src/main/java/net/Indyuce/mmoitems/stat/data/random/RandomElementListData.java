package net.Indyuce.mmoitems.stat.data.random;

import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.ElementListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.ElementStatType;
import net.Indyuce.mmoitems.util.Pair;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RandomElementListData implements RandomStatData<ElementListData>, UpdatableRandomStatData<ElementListData> {
    private final Map<Pair<Element, ElementStatType>, NumericStatFormula> stats = new LinkedHashMap<>();

    public RandomElementListData(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        for (Element element : Element.values())
            for (ElementStatType statType : ElementStatType.values()) {
                final String path = statType.getConcatenatedConfigPath(element);
                if (config.contains(path))
                    stats.put(Pair.of(element, statType), new NumericStatFormula(config.get(path)));
            }
    }

    public boolean hasStat(Element element, ElementStatType statType) {
        return stats.containsKey(Pair.of(element, statType));
    }

    @NotNull
    public NumericStatFormula getStat(Element element, ElementStatType statType) {
        return stats.getOrDefault(Pair.of(element, statType), NumericStatFormula.ZERO);
    }

    public Set<Pair<Element, ElementStatType>> getKeys() {
        return stats.keySet();
    }

    public void setStat(Element element, ElementStatType statType, NumericStatFormula formula) {
        stats.put(Pair.of(element, statType), formula);
    }

    @Override
    public ElementListData randomize(MMOItemBuilder builder) {
        ElementListData elements = new ElementListData();
        stats.forEach((key, value) -> elements.setStat(key.getKey(), key.getValue(), value.calculate(builder.getLevel())));
        return elements;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public ElementListData reroll(@NotNull ItemStat stat, @NotNull ElementListData original, int determinedItemLevel) {

        // Start brand new
        ElementListData elements = new ElementListData();
        ElementListData originalElements = (ElementListData) original;

        // Evaluate each
        for (Element element : Element.values())
            for (ElementStatType statType : ElementStatType.values()) {

                // Whats its
                NumericStatFormula currentTemplateData = getStat(element, statType);
                DoubleData itemData = new DoubleData(originalElements.getStat(element, statType));

                // Evaluate
                DoubleData result = currentTemplateData.reroll(stat, itemData, determinedItemLevel);

                // Apply
                elements.setStat(element, statType, result.getValue());
            }

        // THats it
        return elements;
    }
}