package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores stamina.
 *
 * @author Gunging
 */
public class RestoreStamina extends DoubleStat implements PlayerConsumable {
    public RestoreStamina() {
        super("RESTORE_STAMINA", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "恢复体力", new String[]{"您的消耗品恢复的耐力/力量值"}, new String[]{"consumable"});
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_STAMINA)) return;

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_STAMINA);

        // Any stamina being provided?
        if (d.getValue() != 0)
            PlayerData.get(player).getRPG().giveStamina(d.getValue());
    }
}
