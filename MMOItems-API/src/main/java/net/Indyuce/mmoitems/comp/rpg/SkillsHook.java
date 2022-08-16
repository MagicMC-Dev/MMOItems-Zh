package net.Indyuce.mmoitems.comp.rpg;

import me.leothepro555.skills.database.managers.PlayerInfo;
import me.leothepro555.skills.events.SkillLevelUpEvent;
import me.leothepro555.skills.main.Skills;
import me.leothepro555.skilltype.ScalingType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillsHook implements RPGHandler, Listener {

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
            PlayerInfo info = Skills.get().getPlayerDataManager().loadPlayerInfo(getPlayer());
            return info.getLevel();
        }

        @Override
        public String getClassName() {
            PlayerInfo info = Skills.get().getPlayerDataManager().loadPlayerInfo(getPlayer());
            return info.getSkill().getLanguageName().getDefault();
        }

        @Override
        public double getMana() {
            PlayerInfo info = Skills.get().getPlayerDataManager().loadPlayerInfo(getPlayer());
            return info.getActiveStatType(ScalingType.ENERGY);
        }

        @Override
        public double getStamina() {
            return getPlayer().getFoodLevel();
        }

        @Override
        public void setMana(double value) {
            PlayerInfo info = Skills.get().getPlayerDataManager().loadPlayerInfo(getPlayer());
            info.setActiveStatType(ScalingType.ENERGY, value);
        }

        @Override
        public void setStamina(double value) {
            getPlayer().setFoodLevel((int) value);
        }
    }
}