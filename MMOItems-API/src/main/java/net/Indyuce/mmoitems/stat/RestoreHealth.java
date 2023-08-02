package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores health.
 *
 * @author Gunging
 */
public class RestoreHealth extends DoubleStat implements PlayerConsumable {
    public RestoreHealth() {
        super("RESTORE_HEALTH", VersionMaterial.RED_DYE.toMaterial(), "生命恢复", new String[]{"食用时给予生命恢复"}, new String[]{"consumable"});
    }


    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_HEALTH))
            return;

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_HEALTH);

        // Any health being provided?
        if (d.getValue() != 0)
            MMOUtils.heal(player, d.getValue());
    }
}
