package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores saturation.
 *
 * @author Gunging
 */
public class RestoreSaturation extends DoubleStat implements PlayerConsumable {
    public RestoreSaturation() {
        super("RESTORE_SATURATION", Material.GOLDEN_CARROT, "饱和度恢复", new String[]{"食用时恢复饱和度"}, new String[]{"consumable"});
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {

        /*
         * For some reason, it was in the earlier code that the default value
         * of restored saturation is 6... I am all for backwards compatibility
         * such that this must be respected.
         *
         * 6 is the saturation for a cooked beef. Since 6.7 it now uses the item
         * default saturation modifier using NMS code
         */
        double defSaturation = getSaturationRestored(mmo.getNBT().getItem());
        double saturation = mmo.hasData(ItemStats.RESTORE_SATURATION) ? ((DoubleData) mmo.getData(ItemStats.RESTORE_SATURATION)).getValue() : defSaturation;
        saturation = saturation - (vanillaEating ? defSaturation : 0);

        // Any saturation being provided?
        if (saturation != 0)
            MMOUtils.saturate(player, saturation);
    }

    private float getSaturationRestored(ItemStack item) {
        try {
            return MythicLib.plugin.getVersion().getWrapper().getSaturationRestored(item);
        } catch (Exception ignored) {

            // If it is not a food item
            return 0;
        }
    }
}
