package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores health.
 *
 * @author Gunging
 */
public class RestoreSaturation extends DoubleStat implements PlayerConsumable {
    public RestoreSaturation() {
        super("RESTORE_SATURATION", Material.GOLDEN_CARROT, "Saturation Restoration", new String[]{"Saturation given when consumed."}, new String[]{"consumable"});
    }


    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

        /*
         * For some reason, it was in the earlier code that the default value
         * of restored saturation is 6... I am all for backwards compatibility
         * such that this must be respected.
         */
        int saturation = 6;
        if (mmo.hasData(ItemStats.RESTORE_SATURATION)) {

            // Get value
            DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_SATURATION);
            saturation = SilentNumbers.ceil(d.getValue());
        }

        // Any saturation being provided?
        if (saturation != 0)
            MMOUtils.saturate(player, saturation);
    }
}
