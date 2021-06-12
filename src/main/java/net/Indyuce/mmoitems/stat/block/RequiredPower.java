package net.Indyuce.mmoitems.stat.block;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.DoubleStat;

public class RequiredPower extends DoubleStat {
    public RequiredPower() {
        super("REQUIRED_POWER", Material.IRON_PICKAXE, "Required Pickaxe Power", new String[] { "The required pickaxe power", "needed to break this custom block." }, new String[] { "block" });
    }
}
