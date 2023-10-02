package net.Indyuce.mmoitems.comp.rpg;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.skills.api.events.SkillLevelUpEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;

public class SkillsProHook implements RPGHandler, Listener {

    @EventHandler
    public void a(SkillLevelUpEvent event) {
        OfflinePlayer player = event.getPlayer();
        if (player.isOnline())
            PlayerData.get(player).getInventory().scheduleUpdate();
    }

    @Override
    public void refreshStats(PlayerData data) {
    }

    @Override
    public RPGPlayer getInfo(PlayerData data) {
        return new SkillsPlayer(data);
    }

    public static class SkillsPlayer extends RPGPlayer {
        public SkillsPlayer(PlayerData playerData) {
            super(playerData);
        }

        @Override
        public int getLevel() {
            SkilledPlayer info = SkillsPro.get().getPlayerDataManager().getData(getPlayerData().getPlayer().getUniqueId());
            return info.getLevel();
        }

        @Override
        public String getClassName() {
            SkilledPlayer info = SkillsPro.get().getPlayerDataManager().getData(getPlayerData().getPlayer().getUniqueId());
            return ChatColor.stripColor(info.getSkill().getDisplayName());
        }

        @Override
        public double getMana() {
            SkilledPlayer info = SkillsPro.get().getPlayerDataManager().getData(getPlayerData().getPlayer().getUniqueId());
            return info.getEnergy();
        }

        @Override
        public double getStamina() {
            return getPlayer().getFoodLevel();
        }

        @Override
        public void setMana(double value) {
            SkilledPlayer info = SkillsPro.get().getPlayerDataManager().getData(getPlayerData().getPlayer().getUniqueId());
            info.setEnergy(value);
        }

        @Override
        public void setStamina(double value) {
            getPlayer().setFoodLevel((int) value);
        }
    }
}