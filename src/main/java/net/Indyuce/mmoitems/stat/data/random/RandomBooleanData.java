package net.Indyuce.mmoitems.stat.data.random;

import java.util.Random;

import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomBooleanData implements RandomStatData {
	private final double chance;
	
	private static final Random random = new Random();

	public RandomBooleanData(boolean state) {
		chance = state ? 1 : 0;
	}

	public RandomBooleanData(double chance) {
		this.chance = chance;
	}

	@Override
	public StatData randomize(GeneratedItemBuilder builder) {
		return new BooleanData(random.nextDouble() < chance);
	}
}