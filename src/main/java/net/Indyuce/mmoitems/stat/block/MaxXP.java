package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class MaxXP extends DoubleStat {
    public MaxXP() {
        super("MAX_XP", Material.EXPERIENCE_BOTTLE, "Maximum XP", new String[] { "The maximum xp you will receive", "for breaking this custom block." }, new String[] { "block" });
    }

}