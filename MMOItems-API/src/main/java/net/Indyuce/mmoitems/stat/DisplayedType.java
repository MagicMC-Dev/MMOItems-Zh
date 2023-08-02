package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class DisplayedType extends StringStat implements GemStoneStat {
    public DisplayedType() {
        super("DISPLAYED_TYPE", VersionMaterial.OAK_SIGN.toMaterial(), "显示类型", new String[]{"此选项只会影响物品标注中显示的类型"}, new String[]{"all"});
    }
}
