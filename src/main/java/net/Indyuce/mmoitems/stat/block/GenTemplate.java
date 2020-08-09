package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GenTemplate extends StringStat {
    public GenTemplate() {
        super("GEN_TEMPLATE", new ItemStack(Material.PAPER), "Gen Template", new String[] { "Can be set to any template", "from gen-templates.yml." }, new String[] { "block" });
    }
}
