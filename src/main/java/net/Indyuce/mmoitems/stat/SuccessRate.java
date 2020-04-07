package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;

public class SuccessRate extends DoubleStat implements ProperStat {

	/*
	 * in a different class because Success Rate is meant to be a proper stat
	 */
	public SuccessRate() {
		super("SUCCESS_RATE", new ItemStack(Material.EMERALD), "Success Rate", new String[] { "The chance of your gem to successfully",
				"apply onto an item. This value is 100%", "by default. If it is not successfully", "applied, the gem stone will be lost." },
				new String[] { "gem_stone", "skin" });
	}
}
