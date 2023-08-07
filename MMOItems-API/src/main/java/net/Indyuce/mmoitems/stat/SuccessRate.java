package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;

public class SuccessRate extends DoubleStat implements GemStoneStat {

	/*
	 * in a different class because Success Rate is meant to be a proper stat
	 */
	public SuccessRate() {
		super("SUCCESS_RATE", Material.EMERALD, "成功率", new String[] { "您的宝石/皮肤成功应用于物品的机会", "该值默认为 100%如果应用不成功，", "宝石/皮肤将会丢失" },
				new String[] { "gem_stone", "skin" });
	}
}
