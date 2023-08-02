package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.stat.type.TemplateOption;

public class CraftingPermission extends StringStat implements TemplateOption, GemStoneStat {
    public CraftingPermission() {
        super("CRAFT_PERMISSION", VersionMaterial.OAK_SIGN.toMaterial(), "制作配方权限",
                new String[]{"制作此物品所需的权限改变这个值需要 &o/mi reload recipes"},
                new String[]{"all"});
    }
}