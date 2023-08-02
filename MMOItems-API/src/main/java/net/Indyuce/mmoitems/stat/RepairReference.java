package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class RepairReference extends StringStat implements GemStoneStat {
	public RepairReference() {
		super("REPAIR_TYPE", Material.ANVIL, "维修参考", new String[]{"如果物品有修复参照, 则只能由具有相同修复参照的消耗品修复, 反之亦然"}, new String[]{"all"});
	}
}
