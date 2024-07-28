package net.Indyuce.mmoitems.comp.mmocore.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.RequiredLevelStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

public class RequiredProfession extends RequiredLevelStat {
    private final Profession profession;

    public RequiredProfession(Profession profession) {
        super(true, "PROFESSION_" + UtilityMethods.enumName(profession.getId()), Material.PINK_DYE, profession.getName() + " Level (MMOCore)", new String[]{"Amount of " + profession.getName() + " levels the", "player needs to use the item."});

        this.profession = profession;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        final int requirement = item.getInteger(this.getNBTPath());
        if (requirement <= 0) return true;

        final PlayerData mmocorePlayerData = PlayerData.get(player.getPlayer());
        if (mmocorePlayerData.getCollectionSkills().getLevel(this.profession) >= requirement) return true;

        if (message) {
            Message.NOT_ENOUGH_PROFESSION.format(ChatColor.RED, "#profession#", profession.getName()).send(player.getPlayer());
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
        }
        return false;
    }
}
