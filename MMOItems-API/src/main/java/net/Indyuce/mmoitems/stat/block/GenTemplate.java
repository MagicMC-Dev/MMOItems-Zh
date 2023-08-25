package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;

public class GenTemplate extends StringStat {
    public GenTemplate() {
        super("GEN_TEMPLATE", Material.PAPER, "生成模板", new String[] { "可以设置为 gen-templates.yml", "中的任何模板" }, new String[] { "block" });
    }
}
