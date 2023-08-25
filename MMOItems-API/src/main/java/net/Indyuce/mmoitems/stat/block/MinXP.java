package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class MinXP extends DoubleStat {
    public MinXP() {
        super("MIN_XP", Material.EXPERIENCE_BOTTLE, "最小经验值", new String[] { "打破此自定义方块后", "您将获得的最小经验值" }, new String[] { "block" });
    }
}
