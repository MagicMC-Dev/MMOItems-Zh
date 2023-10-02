package net.Indyuce.mmoitems.comp.mmocore.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class RequiredAttribute extends DoubleStat implements ItemRestriction, GemStoneStat {
    private final PlayerAttribute attribute;

    // TODO merge with RequiredLevelStat
    public RequiredAttribute(PlayerAttribute attribute) {
        super("REQUIRED_" + attribute.getId().toUpperCase().replace("-", "_"), VersionMaterial.GRAY_DYE.toMaterial(), attribute.getName() + " Requirement (MMOCore)", new String[]{"Amount of " + attribute.getName() + " points the", "player needs to use the item."}, new String[]{"!block", "all"});

        this.attribute = attribute;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        final PlayerData mmocorePlayerData = PlayerData.get(player.getPlayer());
        if (mmocorePlayerData.getAttributes().getAttribute(attribute) < item.getStat(getId())) {
            if (message) {
                Message.NOT_ENOUGH_ATTRIBUTE.format(ChatColor.RED, "#attribute#", attribute.getName()).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }
        return true;
    }
}
