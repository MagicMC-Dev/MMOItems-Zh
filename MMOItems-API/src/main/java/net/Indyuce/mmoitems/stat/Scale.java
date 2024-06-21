package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class Scale extends DoubleStat {
    public Scale() {
        super("SCALE", Material.STONE, "尺寸", new String[]{"玩家大小", "默认值为 1"});
    }

    @Override
    public double multiplyWhenDisplaying() {
        return 100;
    }
}
