package net.Indyuce.mmoitems.api.itemgen.tier;

import net.Indyuce.mmoitems.api.ItemTier;

public class RolledTier {
	private final ItemTier tier;
	private final double capacity;

	public RolledTier(RandomTierInfo info, int itemLevel) {
		this.tier = info.getTier();
		this.capacity = info.getCapacity().calculate(itemLevel);
	}

	public boolean isDefault() {
		return tier == null;
	}

	public ItemTier getTier() {
		return tier;
	}

	public double getCapacity() {
		return capacity;
	}
}
