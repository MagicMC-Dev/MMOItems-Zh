package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class RequirePowerToBreak extends BooleanStat {
    public RequirePowerToBreak() {
        super("REQUIRE_POWER_TO_BREAK", Material.BEDROCK, "Require Power to Break", new String[] { "If you need the required pickaxe", "power to break this custom block." }, new String[] { "block" });
    }
}
