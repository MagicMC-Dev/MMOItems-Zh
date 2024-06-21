package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;

public class HideDurabilityBar extends BooleanStat {
    public HideDurabilityBar() {
        super("DURABILITY_BAR", Material.DAMAGED_ANVIL, "隐藏耐久条",
                new String[] { "启用此选项可在使用自定义耐久时隐藏耐久", "使用原版耐久时无法启用" }, new String[] { "!block", "all"});
    }
}
