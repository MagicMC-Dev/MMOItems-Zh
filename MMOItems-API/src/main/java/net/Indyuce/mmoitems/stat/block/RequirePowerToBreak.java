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
        super("REQUIRE_POWER_TO_BREAK", Material.BEDROCK, "需要才能打破", new String[]{"启用后，如果玩家没有足够的挖掘等级，则方块不会破裂，这与普通方块行为不同"}, new String[]{"block"});
    }
}
