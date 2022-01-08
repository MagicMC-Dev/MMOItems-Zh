package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import net.Indyuce.mmoitems.stat.type.AttributeStat;

public class MaxHealth extends AttributeStat {
	public MaxHealth() {
		super("MAX_HEALTH", Material.GOLDEN_APPLE, "Max Health",
				new String[] { "The amount of health your", "item gives to the holder." }, Attribute.GENERIC_MAX_HEALTH);
	}
}
