package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class KnockbackResistance extends DoubleStat {
	public KnockbackResistance() {
		super("KNOCKBACK_RESISTANCE", Material.CHAINMAIL_CHESTPLATE, "击退抗性", new String[] {
				"你的物品阻止爆炸、苦力怕造成的击退的几率...", "1.0 对应 100%、0.7 至 70%..." });
	}

	@Override
	public double multiplyWhenDisplaying() {
		return 100;
	}
}
