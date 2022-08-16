package net.Indyuce.mmoitems.stat.data.random;

import java.util.Random;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomBooleanData implements RandomStatData<BooleanData> {
	private final double chance;

	private static final Random random = new Random();

	public RandomBooleanData(boolean state) {
		chance = state ? 1 : 0;
	}

	public RandomBooleanData(double chance) {
		this.chance = chance;
	}
	
	public double getChance() {
		return chance;
	}

	@Override
	public BooleanData randomize(MMOItemBuilder builder) {
		return new BooleanData(random.nextDouble() < chance);
	}
}