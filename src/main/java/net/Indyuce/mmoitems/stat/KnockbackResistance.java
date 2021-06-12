package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import net.Indyuce.mmoitems.stat.type.AttributeStat;

public class KnockbackResistance extends AttributeStat {
	public KnockbackResistance() {
		super("KNOCKBACK_RESISTANCE", Material.CHAINMAIL_CHESTPLATE, "Knockback Resistance", new String[] {
				"The chance of your item to block the", "knockback from explosions, creepers...", "1.0 corresponds to 100%, 0.7 to 70%..." },
				Attribute.GENERIC_KNOCKBACK_RESISTANCE);
	}

	@Override
	public double multiplyWhenDisplaying() {
		return 100;
	}
}
