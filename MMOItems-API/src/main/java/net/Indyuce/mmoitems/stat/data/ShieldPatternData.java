package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShieldPatternData implements StatData, RandomStatData<ShieldPatternData> {
    private DyeColor base;
    private final List<Pattern> patterns = new ArrayList<>();

    public ShieldPatternData(DyeColor base, Pattern... patterns) {
        this.base = base;
        this.patterns.addAll(Arrays.asList(patterns));
    }

    public void setBase(@Nullable DyeColor base) {
        this.base = base;
    }

    @Nullable
    public DyeColor getBaseColor() {
        return base;
    }

    @NotNull
    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void add(Pattern pattern) {
        patterns.add(pattern);
    }

    public void addAll(List<Pattern> patterns) {
        this.patterns.addAll(patterns);
    }

    @Override
    public ShieldPatternData clone() {
        final ShieldPatternData clone = new ShieldPatternData(base);
        clone.patterns.addAll(patterns);
        return clone;
    }

    @Override
    public boolean isEmpty() {
        return base == null && patterns.isEmpty();
    }

    @Override
    public ShieldPatternData randomize(MMOItemBuilder builder) {
        return this;
    }
}