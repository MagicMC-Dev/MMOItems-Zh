package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class RepairReference extends StringStat implements GemStoneStat {
	public RepairReference() {
		super("REPAIR_TYPE", Material.ANVIL, "Repair Reference", new String[]{"If items have a repair reference, they can", "only be repaired by consumables", "with the same repair reference,", "and vice-versa."}, new String[]{"all"});
	}
}
