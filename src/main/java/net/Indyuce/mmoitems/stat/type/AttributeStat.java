package net.Indyuce.mmoitems.stat.type;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

public abstract class AttributeStat extends DoubleStat {

	/*
	 * attribute offset for e.g attack speed must be lowered when holding a
	 * weapon by 4 because this is the default base attribute value.
	 */
	private final double offset;
	private final Attribute attribute;

	public AttributeStat(ItemStack item, String name, String[] lore, String path, Attribute attribute) {
		this(item, name, lore, path, attribute, 0);
	}

	public AttributeStat(ItemStack item, String name, String[] lore, String path, Attribute attribute, double offset) {
		super(item, name, lore, path, new String[] { "!consumable", "!miscellaneous", "all" });

		this.offset = offset;
		this.attribute = attribute;
	}

	public boolean isWeaponSpecific() {
		return offset > 0;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	@Deprecated
	public double getOffset() {
		return offset;
	}
}
