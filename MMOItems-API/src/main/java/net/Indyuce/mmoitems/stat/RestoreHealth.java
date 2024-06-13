package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores health.
 *
 * @author Gunging
 */
public class RestoreHealth extends DoubleStat implements PlayerConsumable {
    public RestoreHealth() {
        super("RESTORE_HEALTH", Material.RED_DYE, "生命恢复", new String[]{"食用时给予生命恢复"}, new String[]{"consumable"});
    }


    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {
        if (!mmo.hasData(ItemStats.RESTORE_HEALTH)) return;

        final DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_HEALTH);
        if (d.getValue() != 0) MMOUtils.heal(player, d.getValue());
    }
}
