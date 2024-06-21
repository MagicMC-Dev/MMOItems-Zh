package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 2})
public class MaxAbsorption extends DoubleStat {
    public MaxAbsorption() {
        super("MAX_ABSORPTION", Material.ENCHANTED_GOLDEN_APPLE,
                "伤害吸收最大值", new String[]{"并不提供永久的伤害吸收效果"，"而是増加你在任何时候"，"可以拥有的最大吸收之（黄色心）心数量"});
    }
}
