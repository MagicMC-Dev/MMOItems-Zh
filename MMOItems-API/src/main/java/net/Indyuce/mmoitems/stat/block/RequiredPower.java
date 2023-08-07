package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class RequiredPower extends DoubleStat {
    public RequiredPower() {
        super("REQUIRED_POWER", Material.IRON_PICKAXE, "所需挖掘等级", new String[] { "打破这个自定义方块所需的挖掘等级" }, new String[] { "block" });
    }
}
