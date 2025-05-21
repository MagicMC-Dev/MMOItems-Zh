package net.Indyuce.mmoitems.paper;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.server.ServerAdapter;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PaperServerAdapter implements ServerAdapter {

    @Override
    public DoubleStat consumableConsumeSeconds() {
        if (MythicLib.plugin.getVersion().isUnder(1, 21, 4)) return new DummyDoubleStat();
        return new ConsumableConsumeSeconds();
    }

    /**
     * TODO proper support for components
     */
    @Deprecated
    @Override
    public void setDisplayName(ItemStack item, ItemMeta meta, String rawNameFormat) {
        Component initial = meta.displayName() != null ? meta.displayName() : Component.text(MMOUtils.fancyName(item.getType()));
        Component newest = MiniMessageHelper.deserialize(rawNameFormat).replaceText(TextReplacementConfig.builder().match("{name}").replacement(initial).build());
        meta.displayName(newest);
    }
}
