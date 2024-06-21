package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VMaterial;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;

@VersionDependant(version = {1, 20, 5})
public class BlockInteractionRange extends DoubleStat {
    public BlockInteractionRange() {
        super("BLOCK_INTERACTION_RANGE", VMaterial.SPYGLASS.get(),
                "方块交互距离", new String[]{"打破方块或与方块交互的距离。", "在创造模式下默认值为 5，", "生存模式下默认值为 4.5"});
    }
}
