package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class RepairReference extends StringStat implements GemStoneStat {
    public RepairReference() {
        super("REPAIR_TYPE", Material.ANVIL, "维修参考(限定)物品", new String[]{"如果物品有修理参照物", "则只能由具有相同修理", "参照物的消耗品修理,", "反之亦然(请参照Wiki)"}, new String[]{"all"});
    }
}
