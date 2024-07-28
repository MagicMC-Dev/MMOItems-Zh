package net.Indyuce.mmoitems.comp.mmocore.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.RequiredLevelStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

public class RequiredAttribute extends RequiredLevelStat {
    private final PlayerAttribute attribute;

    public RequiredAttribute(PlayerAttribute attribute) {
        super(UtilityMethods.enumName(attribute.getId()), Material.GRAY_DYE, attribute.getName() + " (MMOCore)", new String[]{"Amount of " + attribute.getName() + " points the", "player needs to use the item."});

        this.attribute = attribute;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        final int requirement = item.getInteger(this.getNBTPath());
        if (requirement <= 0) return true;

        final PlayerData mmocorePlayerData = PlayerData.get(player.getPlayer());
        if (mmocorePlayerData.getAttributes().getAttribute(attribute) >= requirement) return true;

        if (message) {
            Message.NOT_ENOUGH_ATTRIBUTE.format(ChatColor.RED, "#attribute#", attribute.getName()).send(player.getPlayer());
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
        }
        return false;
    }
}
