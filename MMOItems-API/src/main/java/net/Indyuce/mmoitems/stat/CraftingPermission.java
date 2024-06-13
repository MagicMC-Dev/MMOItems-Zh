package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.stat.type.TemplateOption;
import org.bukkit.Material;

public class CraftingPermission extends StringStat implements TemplateOption, GemStoneStat {
    public CraftingPermission() {
        super("CRAFT_PERMISSION", Material.OAK_SIGN, "制作配方权限",
                new String[]{"制作此物品所需的权限，更改此", "设置需要使用 &o/mi reload recipes"},
                new String[0]);
    }
}