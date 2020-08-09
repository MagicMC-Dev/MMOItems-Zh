package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MinXP extends DoubleStat {
    public MinXP() {
        super("MIN_XP", new ItemStack(Material.EXPERIENCE_BOTTLE), "Minimum XP", new String[] { "The minimum xp you will receive", "for breaking this custom block." }, new String[] { "block" });
    }
}
