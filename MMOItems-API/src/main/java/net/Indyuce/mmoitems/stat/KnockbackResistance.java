package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class KnockbackResistance extends DoubleStat {
	public KnockbackResistance() {
		super("KNOCKBACK_RESISTANCE", Material.CHAINMAIL_CHESTPLATE, "Knockback Resistance", new String[] {
				"The chance of your item to block the", "knockback from explosions, creepers...", "1.0 corresponds to 100%, 0.7 to 70%..." });
	}

	@Override
	public double multiplyWhenDisplaying() {
		return 100;
	}
}
