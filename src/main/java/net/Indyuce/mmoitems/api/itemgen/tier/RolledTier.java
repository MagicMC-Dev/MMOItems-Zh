package net.Indyuce.mmoitems.api.itemgen.tier;

import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;

public class RolledTier {
	private final ItemTier tier;
	private final double capacity;

	public RolledTier(RandomTierInfo info, GeneratedItemBuilder builder) {
		this.tier = info.getTier();
		this.capacity = info.getCapacity().calculate(builder.getLevel());
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
