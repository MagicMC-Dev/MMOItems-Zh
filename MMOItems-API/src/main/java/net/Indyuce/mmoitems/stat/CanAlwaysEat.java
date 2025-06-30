package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jules
 */
@VersionDependant(version = {1, 20, 4})
public class CanAlwaysEat extends BooleanStat implements GemStoneStat {
    public CanAlwaysEat() {
        super("CAN_ALWAYS_EAT", Material.COOKED_CHICKEN, "Can always eat?", new String[]{"If true, this item can be eaten", "even if the player is not hungry.", "Available only on 1.20.4+"}, new String[]{"consumable"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
        if (data.isEnabled()) {
            FoodComponent comp = item.getMeta().getFood();
            comp.setCanAlwaysEat(true);
            item.getMeta().setFood(comp);
        }
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (meta.hasFood()) mmoitem.setData(this, new BooleanData(meta.getFood().canAlwaysEat()));
    }
}
