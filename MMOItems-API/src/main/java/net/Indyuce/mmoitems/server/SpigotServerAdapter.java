package net.Indyuce.mmoitems.server;

import net.Indyuce.mmoitems.stat.ConsumableConsumeSeconds;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpigotServerAdapter implements ServerAdapter {

    @Override
    public DoubleStat consumableConsumeSeconds() {
        return new ConsumableConsumeSeconds();
    }

    @Deprecated
    @Override
    public void setDisplayName(ItemStack item, ItemMeta meta, String rawNameFormat) {
        String oldName = meta.hasDisplayName() ? meta.getDisplayName() : MMOUtils.fancyName(item.getType());
        meta.setDisplayName(rawNameFormat.replace("{name}", oldName));
    }
}
