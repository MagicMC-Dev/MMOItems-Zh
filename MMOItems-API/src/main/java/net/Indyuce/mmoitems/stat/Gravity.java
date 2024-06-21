package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class Gravity extends DoubleStat {
    public Gravity() {
        super("GRAVITY", Material.STONE,
                "重力", new String[]{"增加重力。默认值为1"});
    }

    @Override
    public double multiplyWhenDisplaying() {
        return 100;
    }
}
