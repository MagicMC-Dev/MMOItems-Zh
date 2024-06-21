package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class StepHeight extends DoubleStat {
    public StepHeight() {
        super("STEP_HEIGHT", Material.GOLDEN_BOOTS,
                "坡度", new String[]{"行走或冲刺时，不需要跳跃就可以越过","的额外方块数。默认值是0.6，即仅高于一个半砖。"});
    }
}
