package net.Indyuce.mmoitems.stat.block;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BlockID extends DoubleStat {

    public BlockID() {
        super("BLOCK_ID", new ItemStack(Material.STONE), "Block ID", new String[] { "This value determines which", "custom block will get placed." }, new String[] { "block" });
    }

    @Override
    public void whenApplied(ItemStackBuilder item, StatData data) {
        super.whenApplied(item, data);
        item.addItemTag(new ItemTag("CustomModelData", (int) ((DoubleData) data).generateNewValue() +1000));
    }
}
