package net.Indyuce.mmoitems.api.drop;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class DropItem {
	private final Type type;
	private final String id;
	private final double unidentification, drop;
	private final int min, max;

	private static final Random random = new Random();

	/*
	 * used in MythicDrops drop tables.
	 */
	public DropItem(Type type, String id, double unidentification) {
		this(type, id, 100, unidentification, 1, 1);
	}

	public DropItem(Type type, String id, double drop, double unidentification, int min, int max) {
		this.type = type;
		this.id = id;

		this.drop = drop;
		this.unidentification = unidentification;
		this.min = min;
		this.max = max;
	}

	/*
	 * used when loading drop tables from drops.yml
	 */
	public DropItem(Type type, String id, String info) throws Exception {
		this.type = type;
		this.id = id;

		String[] argSplit = info.split("\\,");
		drop = Double.parseDouble(argSplit[0]) / 100;

		String[] amountSplit = argSplit[1].split("\\-");
		min = Integer.parseInt(amountSplit[0]);
		max = amountSplit.length > 1 ? Integer.parseInt(amountSplit[1]) : min;

		unidentification = Double.parseDouble(argSplit[2]) / 100;
	}

	public boolean isDropped() {
		return random.nextDouble() < drop;
	}

	private boolean isUnidentified() {
		return random.nextDouble() < unidentification;
	}

	private int getRandomAmount() {
		return max > min ? min + random.nextInt(max - min + 1) : min;
	}

	public ItemStack getItem() {
		return getItem(getRandomAmount());
	}

	public ItemStack getItem(int amount) {
		ItemStack item = MMOItems.plugin.getItems().getItem(type, id);
		if (item == null || item.getType() == Material.AIR)
			return null;

		item.setAmount(amount);
		return isUnidentified() ? type.getUnidentifiedTemplate().newBuilder(MMOItems.plugin.getNMS().getNBTItem(item)).build() : item;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}
}
