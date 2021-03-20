package net.Indyuce.mmoitems.api.util;

import java.util.Random;

public class RandomAmount {
	private final int min, max;

	private static final Random RANDOM = new Random();

	public RandomAmount(int min, int max) {
		this.min = min;
		this.max = max;
	}

	/**
	 * Reads random amount from a string formatted "{min}-{max}"
	 * 
	 * @param str
	 *            String to extract the random amount from
	 */
	public RandomAmount(String str) {
		String[] split = str.split("-");
		min = Integer.parseInt(split[0]);
		max = split.length > 1 ? Integer.parseInt(split[1]) : 0;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getRandomAmount() {
		return max > 0 ? min + RANDOM.nextInt(max - min + 1) : min;
	}
}
