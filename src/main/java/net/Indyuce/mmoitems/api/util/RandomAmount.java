package net.Indyuce.mmoitems.api.util;

import java.util.Random;

public class RandomAmount {
	private final int min, max;

	private static final Random random = new Random();

	public RandomAmount(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public RandomAmount(String str) {
		String[] split = str.split("\\-");
		min = Integer.valueOf(split[0]);
		max = split.length > 1 ? Integer.valueOf(split[1]) : 0;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getRandomAmount() {
		return max > 0 ? min + random.nextInt(max - min + 1) : min;
	}
}
