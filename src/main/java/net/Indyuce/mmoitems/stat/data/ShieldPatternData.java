package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class ShieldPatternData implements StatData, RandomStatData {
	private final DyeColor base;
	private final List<Pattern> patterns = new ArrayList<>();

	public ShieldPatternData(DyeColor base, Pattern... patterns) {
		this.base = base;
		for (Pattern pattern : patterns)
			this.patterns.add(pattern);
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
	public StatData randomize(MMOItemBuilder builder) {
		return this;
	}
}