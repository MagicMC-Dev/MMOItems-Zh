package net.Indyuce.mmoitems.server;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface ServerAdapter {

    DoubleStat consumableConsumeSeconds();

    @Deprecated
    public void setDisplayName(ItemStack item, ItemMeta meta, String rawNameFormat);

    public static ServerAdapter paper() {
        try {
            Class<?> paperClass = Class.forName("net.Indyuce.mmoitems.paper.PaperServerAdapter");
            Object instance = paperClass.getDeclaredConstructor().newInstance();
            return (ServerAdapter) instance;
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not enable Paper support", throwable);
        }
    }
}
