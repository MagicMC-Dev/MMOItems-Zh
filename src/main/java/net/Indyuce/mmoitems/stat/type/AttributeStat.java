package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

/**
 * Attribute stats are also collected when registered in the StatManager because
 * their corresponding player vanilla attributes must be updated when the player
 * stat value changes
 * 
 * @author cympe
 *
 */
public abstract class AttributeStat extends DoubleStat {

	/**
	 * Attribute offset for e.g attack speed must be lowered when holding a
	 * weapon by 4 because this is the default base attribute value.
	 */
	private final double offset;

	private final Attribute attribute;

	public AttributeStat(String id, Material mat, String name, String[] lore, Attribute attribute) {
		this(id, mat, name, lore, attribute, 0);
	}

	public AttributeStat(String id, Material mat, String name, String[] lore, Attribute attribute, double offset) {
		super(id, mat, name, lore, new String[] { "!consumable", "!block", "!miscellaneous", "all" });

		this.offset = offset;
		this.attribute = attribute;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public double getOffset() {
		return offset;
	}
}
