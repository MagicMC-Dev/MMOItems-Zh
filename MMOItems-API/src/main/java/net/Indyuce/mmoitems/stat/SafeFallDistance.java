package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class SafeFallDistance extends DoubleStat {
    public SafeFallDistance() {
        super("SAFE_FALL_DISTANCE", Material.RED_BED, "摔落高度", new String[]{"不受到摔落伤害的高度", "（玩家跳下时，不会受到摔落伤害）", "默认值为3"});
    }
}
