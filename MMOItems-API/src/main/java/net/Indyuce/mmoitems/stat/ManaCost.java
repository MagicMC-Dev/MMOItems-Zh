package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * mmoitems
 *
 * @author Roch Blondiaux
 * @date 24/10/2022
 */
public class ManaCost extends DoubleStat implements ItemRestriction, PlayerConsumable {

    public ManaCost() {
        super("MANA_COST", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Mana Cost", new String[]{"Mana spent by your weapon to be used."}, new String[]{"piercing", "slashing", "blunt", "range"});
    }


    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        // No data no service
        if (!item.hasTag(ItemStats.MANA_COST.getNBTPath()))
            return true;
        double manaCost = item.getDouble(ItemStats.MANA_COST.getNBTPath());
        boolean hasMana = manaCost > 0 && player.getMana() >= manaCost;
        if (!hasMana)
            Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(player.getPlayer());
        return hasMana;
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {
        // No data no service
        if (!mmo.hasData(ItemStats.MANA_COST)) return;

        // Get value
        DoubleData d = (DoubleData) mmo.getData(ItemStats.MANA_COST);
        if (d.getValue() > 0) {
            final RPGPlayer rpgPlayer = PlayerData.get(player).getRPG();
            rpgPlayer.setMana(rpgPlayer.getMana() - d.getValue());
        }
    }
}
