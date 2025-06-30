package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.consumable.ConsumableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jules
 */

@VersionDependant(version = {1, 21, 2})
public class ConsumableConsumeSeconds extends DoubleStat implements GemStoneStat {
    public ConsumableConsumeSeconds() {
        super("CONSUME_SECONDS", Material.CLOCK, "Consume Seconds", new String[]{"Time needed (in seconds) to eat the item.", "Available only on 1.21.4+"}, new String[]{"consumable"});

        // Paper 1.21.4 does not implement that method!!!
        try {
            ItemMeta.class.getMethod("getConsumable");
        } catch(Throwable throwable) {
            disable();
        }
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        if (data.getValue() >= 0) {
            ConsumableComponent comp = item.getMeta().getConsumable();
            comp.setConsumeSeconds((float) data.getValue());
            item.getMeta().setConsumable(comp);
        }
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (meta.hasConsumable()) {
            ConsumableComponent comp = meta.getConsumable();
            mmoitem.setData(this, new DoubleData(comp.getConsumeSeconds()));
        }
    }
}
