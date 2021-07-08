package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
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
public class RestoreFood extends DoubleStat implements PlayerConsumable {
    public RestoreFood() {
        super("RESTORE_FOOD", VersionMaterial.PORKCHOP.toMaterial(), "Food Restoration", new String[]{"Food units given when consumed."}, new String[]{"consumable"});
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_FOOD))
            return;

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_FOOD);

        // Any health being provided?
        if (d.getValue() != 0)
            MMOUtils.feed(player, SilentNumbers.ceil(d.getValue()));
    }
}