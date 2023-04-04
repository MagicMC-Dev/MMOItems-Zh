package net.Indyuce.mmoitems.comp.rpg;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerLevelUpEvent;
import com.sucy.skill.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillAPIHook implements RPGHandler, Listener {

    @Override
    public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return new SkillAPIPlayer(data);
    }

    @EventHandler
    public void b(PlayerLevelUpEvent event) {
        net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayerData().getPlayer()).getInventory().scheduleUpdate();
    }

    @Override
    public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
    }

    public static class SkillAPIPlayer extends RPGPlayer {
        public SkillAPIPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
            super(playerData);
        }

        @Override
        public int getLevel() {
            PlayerData rpgdata = SkillAPI.getPlayerData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMainClass().getLevel() : 0;
        }

        @Override
        public String getClassName() {
            PlayerData rpgdata = SkillAPI.getPlayerData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMainClass().getData().getName() : "";
        }

        @Override
        public double getMana() {
            PlayerData rpgdata = SkillAPI.getPlayerData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMana() : 0;
        }

        @Override
        public double getStamina() {
            return getPlayer().getFoodLevel();
        }

        @Override
        public void setMana(double value) {
            PlayerData rpgdata = SkillAPI.getPlayerData(getPlayer());
            if (rpgdata.hasClass())
                rpgdata.setMana(value);
        }

        @Override
        public void setStamina(double value) {
            getPlayer().setFoodLevel((int) value);
        }
    }
}