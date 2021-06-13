package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;

public class DisableDeathDrop extends BooleanStat {
    public DisableDeathDrop() {
        super("DISABLE_DEATH_DROP", Material.BONE, "Disable Drop On Death",
                new String[] { "Enable this to prevent this item", "from dropping on the wielder's death." }, new String[] { "all" });
    }
}
