package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;

public class SuccessRate extends DoubleStat implements GemStoneStat {

	/*
	 * in a different class because Success Rate is meant to be a proper stat
	 */
	public SuccessRate() {
		super("SUCCESS_RATE", Material.EMERALD, "宝石/皮肤应用(使用)成功概率", new String[] { "您的宝石/皮肤成功应用(使用)到物品上的几率.", "该值默认为 100%", "如果未成功应用(使用)", "宝石/皮肤将消失" },
				new String[] { "gem_stone", "skin" });
	}
}
