package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class ShieldPatternData implements StatData, RandomStatData<ShieldPatternData> {
	private final DyeColor base;
	private final List<Pattern> patterns = new ArrayList<>();

	public ShieldPatternData(DyeColor base, Pattern... patterns) {
		this.base = base;
		this.patterns.addAll(Arrays.asList(patterns));
	}

	public DyeColor getBaseColor() {
		return base;
	}

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
	public ShieldPatternData randomize(MMOItemBuilder builder) {
		return this;
	}
}