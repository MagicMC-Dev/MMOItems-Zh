package net.Indyuce.mmoitems.comp.mmocore.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

public class RequiredProfession extends DoubleStat implements ItemRestriction, GemStoneStat {
    private final Profession profession;

    // TODO merge with RequiredLevelStat
    public RequiredProfession(Profession profession) {
        super("PROFESSION_" + profession.getId().toUpperCase().replace("-", "_"), Material.PINK_DYE, profession.getName() + " Requirement (MMOCore)",
                new String[]{"Amount of " + profession.getName() + " levels the", "player needs to use the item."}, new String[]{"!block", "all"});

        this.profession = profession;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        final PlayerData mmocorePlayerData = PlayerData.get(player.getPlayer());
        if (mmocorePlayerData.getCollectionSkills().getLevel(this.profession) < item.getStat(getId())) {
            if (message) {
                Message.NOT_ENOUGH_PROFESSION.format(ChatColor.RED, "#profession#", profession.getName()).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }
        return true;
    }
}
