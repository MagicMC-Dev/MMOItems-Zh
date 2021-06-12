package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.BooleanStat;

public class DurabilityBar extends BooleanStat {
    public DurabilityBar() {
        super("DURABILITY_BAR", Material.DAMAGED_ANVIL, "Hide Durability Bar",
                new String[] { "Enable this to have the green bar", "hidden when using custom durability." }, new String[] { "!block", "all"});
    }
}
