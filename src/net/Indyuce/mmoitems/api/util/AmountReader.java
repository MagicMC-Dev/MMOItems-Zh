package net.Indyuce.mmoitems.api.util;

import java.util.Random;

public class AmountReader {
	private int min, max = 0;
	private boolean isValid = true;

	private static final Random random = new Random();

	public AmountReader(String str) {
		String[] split = str.split("\\-");

		try {
			min = Integer.valueOf(split[0]);
		} catch (Exception e) {
			isValid = false;
			return;
		}

		if (split.length > 1)
			try {
				max = Integer.valueOf(split[1]);
			} catch (Exception e) {
				isValid = false;
				return;
			}
	}

	public boolean isValid() {
		return isValid;
	}

	public int getRandomAmount() {
		return max > 0 ? min + random.nextInt(max - min + 1) : min;
	}
}
