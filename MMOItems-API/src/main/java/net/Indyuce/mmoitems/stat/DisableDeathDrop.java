package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;

public class DisableDeathDrop extends BooleanStat {
    public DisableDeathDrop() {
        super("DISABLE_DEATH_DROP", Material.BONE, "禁用死亡掉落",
                new String[] { "启用此选项可以防止该物", "品在持有者死亡时掉落" }, new String[] { "all" });
    }
}
