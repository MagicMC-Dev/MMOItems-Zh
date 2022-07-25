package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class DisplayedType extends StringStat implements GemStoneStat {
    public DisplayedType() {
        super("DISPLAYED_TYPE", VersionMaterial.OAK_SIGN.toMaterial(), "Displayed Type", new String[]{"This option will only affect the", "type displayed on the item lore."}, new String[]{"all"});
    }
}
