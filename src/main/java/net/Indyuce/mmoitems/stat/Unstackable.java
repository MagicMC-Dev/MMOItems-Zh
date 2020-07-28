package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Unstackable extends BooleanStat {

    public Unstackable() {
        super("UNSTACKABLE", new ItemStack(Material.CHEST_MINECART), "Unstackable", new String[] { "This will make the item unable", "to be stacked with itself."}, new String[] { "all" });
    }

    @Override
    public void whenApplied(MMOItemBuilder item, StatData data) {
        if (((BooleanData) data).isEnabled()) {
            item.addItemTag(new ItemTag("UNSTACKABLE", true));
            item.addItemTag(new ItemTag("UNSTACKABLE_UUID", UUID.randomUUID().toString()));
        }
    }
}
