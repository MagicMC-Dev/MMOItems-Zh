package net.Indyuce.mmoitems.comp.rpg;

import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.event.PlayerLevelUpEvent;
import studio.magemonkey.fabled.api.player.PlayerData;

public class FabledHook implements RPGHandler, Listener {

    @Override
    public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return new PlayerWrapper(data);
    }

    @EventHandler
    public void b(PlayerLevelUpEvent event) {
        net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayerData().getPlayer()).getInventory().scheduleUpdate();
    }

    @Override
    public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
    }

    private static class PlayerWrapper extends RPGPlayer {
        public PlayerWrapper(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
            super(playerData);
        }

        @Override
        public int getLevel() {
            PlayerData rpgdata = Fabled.getData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMainClass().getLevel() : 0;
        }

        @Override
        public String getClassName() {
            PlayerData rpgdata = Fabled.getData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMainClass().getData().getName() : "";
        }

        @Override
        public double getMana() {
            PlayerData rpgdata = Fabled.getData(getPlayer());
            return rpgdata.hasClass() ? rpgdata.getMana() : 0;
        }

        @Override
        public double getStamina() {
            return getPlayer().getFoodLevel();
        }

        @Override
        public void setMana(double value) {
            PlayerData rpgdata = Fabled.getData(getPlayer());
            if (rpgdata.hasClass())
                rpgdata.setMana(value);
        }

        @Override
        public void setStamina(double value) {
            getPlayer().setFoodLevel((int) value);
        }
    }
}