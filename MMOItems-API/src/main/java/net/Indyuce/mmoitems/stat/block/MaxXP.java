package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class MaxXP extends DoubleStat {
    public MaxXP() {
        super("MAX_XP", Material.EXPERIENCE_BOTTLE, "最大经验值", new String[] { "打破此自定义方块后", "您将获得的最大经验值" }, new String[] { "block" });
    }

}