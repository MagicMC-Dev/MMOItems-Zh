package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;

/**
 * Vanilla behaviour:
 * When a player tries to break a block but doesn't
 * have enough pickaxe power, the block is broken but
 * does NOT drop anything.
 * <p>
 * This option:
 * When toggled on, the block simply won't break/drop
 */
public class RequirePowerToBreak extends BooleanStat {
    public RequirePowerToBreak() {
        super("REQUIRE_POWER_TO_BREAK", Material.BEDROCK, "Require Power to Break", new String[]{"When enabled, the block will NOT break", "if the player doesn't have enough pickaxe", "power, unlike vanilla block behaviour."}, new String[]{"block"});
    }
}
