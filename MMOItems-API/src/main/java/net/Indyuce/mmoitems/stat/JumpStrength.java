package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;

@VersionDependant(version = {1, 20, 5})
public class JumpStrength extends DoubleStat {
    public JumpStrength() {
        super("JUMP_STRENGTH", Material.SADDLE,
                "跳跃高度", new String[]{"以块为单位的跳跃高度，", "默认值为1.25"});
    }
}
