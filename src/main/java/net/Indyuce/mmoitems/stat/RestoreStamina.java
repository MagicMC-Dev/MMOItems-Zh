package net.Indyuce.mmoitems.stat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.SelfConsumable;

/**
 * When a consumable is eaten, restores health.
 *
 * @author Gunging
 */
public class RestoreStamina extends DoubleStat implements SelfConsumable {
    public RestoreStamina() { super("RESTORE_STAMINA", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Restore Stamina", new String[]{"The amount of stamina/power", "your consumable restores."}, new String[]{"consumable"}); }


    @Override
    public boolean onSelfConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_STAMINA)) { return false; }

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_STAMINA);

        // Any health being provided?
        if (d.getValue() != 0) { PlayerData.get(player).getRPG().giveStamina(d.getValue()); return true; }

        // No health no need to consume
        return false;
    }
}
