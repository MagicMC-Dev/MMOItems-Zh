package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class FallDamageMultiplier extends DoubleStat {
    public FallDamageMultiplier() {
        super("FALL_DAMAGE_MULTIPLIER", Material.DAMAGED_ANVIL,
                "摔落伤害", new String[]{"增加一定百分比的坠落伤害。", "默认值为100%"});
    }

    @Override
    public double multiplyWhenDisplaying() {
        return 100;
    }
}
