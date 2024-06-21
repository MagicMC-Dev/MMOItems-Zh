package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class BlockBreakSpeed extends DoubleStat {
    public BlockBreakSpeed() {
        super("BLOCK_BREAK_SPEED", Material.IRON_PICKAXE,
                "挖掘速度", new String[]{"方块破坏速度", "空手时挖掘速度默认为 1"});
    }
}
