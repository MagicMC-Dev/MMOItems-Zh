package net.Indyuce.mmoitems.api.droptable.item;

import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class DropItem {
	private final double drop;
	private final int min, max;

	protected static final Random random = new Random();

	public DropItem(double drop, int min, int max) {
		this.drop = drop;
		this.min = min;
		this.max = max;
	}

	/*
	 * used when loading drop tables from drops.yml
	 */
	public DropItem(String str) {
		String[] argSplit = str.split(",");
		drop = Double.parseDouble(argSplit[0]) / 100;

		String[] amountSplit = argSplit[1].split("-");
		min = Integer.parseInt(amountSplit[0]);
		max = amountSplit.length > 1 ? Integer.parseInt(amountSplit[1]) : min;
	}

	public boolean rollDrop() {
		return random.nextDouble() < drop;
	}

	public int rollAmount() {
		return max > min ? min + random.nextInt(max - min + 1) : min;
	}

	public ItemStack getItem(@Nullable PlayerData player) {
		return getItem(player, rollAmount());
	}

	public abstract ItemStack getItem(@Nullable PlayerData player, int amount);

	public abstract String getKey();
}
