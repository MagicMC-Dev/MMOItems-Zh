package net.Indyuce.mmoitems.paper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 4})
public class ConsumableConsumeSeconds extends DoubleStat implements GemStoneStat {
    public ConsumableConsumeSeconds() {
        super("CONSUME_SECONDS", Material.CLOCK, "Consume Seconds", new String[]{"Time needed (in seconds) to eat the item.", "Available only on 1.21.4+"}, new String[]{"consumable"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        if (data.getValue() >= 0) {
            final float value = (float) data.getValue();
            item.addFutureActionItemstack(stack -> {
                Consumable comp = Consumable.consumable().consumeSeconds(value).build();
                stack.setData(DataComponentTypes.CONSUMABLE, comp);
            });
        }
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().hasData(DataComponentTypes.CONSUMABLE)) {
            Consumable comp = mmoitem.getNBT().getItem().getData(DataComponentTypes.CONSUMABLE);
            mmoitem.setData(this, new DoubleData(comp.consumeSeconds()));
        }
    }
}
