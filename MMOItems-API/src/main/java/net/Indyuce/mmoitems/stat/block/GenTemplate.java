package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class GenTemplate extends StringStat {
    public GenTemplate() {
        super("GEN_TEMPLATE", Material.PAPER, "Gen Template", new String[] { "Can be set to any template", "from gen-templates.yml." }, new String[] { "block" });
    }
}
