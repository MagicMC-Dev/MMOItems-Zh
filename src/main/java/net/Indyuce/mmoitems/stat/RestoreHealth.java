package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.SelfConsumable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores health.
 *
 * @author Gunging
 */
public class RestoreHealth extends DoubleStat implements SelfConsumable {
    public RestoreHealth() { super("RESTORE_HEALTH", VersionMaterial.RED_DYE.toMaterial(), "Health Restoration", new String[]{"Health given when consumed."}, new String[]{"consumable"}); }


    @Override
    public boolean onSelfConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_HEALTH)) { return false; }

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_HEALTH);

        // Any health being provided?
        if (d.getValue() != 0) { MMOUtils.heal(player, d.getValue()); return true; }

        // No health no need to consume
        return false;
    }
}
