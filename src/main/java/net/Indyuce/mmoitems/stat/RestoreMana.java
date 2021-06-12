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
public class RestoreMana extends DoubleStat implements SelfConsumable {
    public RestoreMana() { super("RESTORE_MANA", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Restore Mana", new String[]{"The amount of mana", "your consumable restores."}, new String[]{"consumable"}); }

    @Override
    public boolean onSelfConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

        // No data no service
        if (!mmo.hasData(ItemStats.RESTORE_MANA)) { return false; }

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.RESTORE_MANA);

        // Any health being provided?
        if (d.getValue() != 0) { PlayerData.get(player).getRPG().giveMana(d.getValue()); return true; }

        // No health no need to consume
        return false;
    }
}