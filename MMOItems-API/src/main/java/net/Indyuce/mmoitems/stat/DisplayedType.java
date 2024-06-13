package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class DisplayedType extends StringStat implements GemStoneStat {
    public DisplayedType() {
        super("DISPLAYED_TYPE", Material.OAK_SIGN, "显示类型", new String[]{"该设置只会影响物品", "lore描述中显示的物品类型"}, new String[0]);
    }
}
