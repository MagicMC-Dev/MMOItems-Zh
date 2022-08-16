package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;

public class SuccessRate extends DoubleStat implements GemStoneStat {

	/*
	 * in a different class because Success Rate is meant to be a proper stat
	 */
	public SuccessRate() {
		super("SUCCESS_RATE", Material.EMERALD, "Success Rate", new String[] { "The chance of your gem/skin to successfully",
				"apply onto an item. This value is 100%", "by default. If it is not successfully", "applied, the gem/skin will be lost." },
				new String[] { "gem_stone", "skin" });
	}
}
