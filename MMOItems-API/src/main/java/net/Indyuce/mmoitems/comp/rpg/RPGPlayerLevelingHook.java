package net.Indyuce.mmoitems.comp.rpg;

import me.baks.rpl.api.API;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class RPGPlayerLevelingHook implements RPGHandler {
    @Override
    public void refreshStats(PlayerData data) {
    }

    @Override
    public RPGPlayer getInfo(PlayerData data) {
        return new RPGPlayerLevelingPlayer(data);
    }

    /*
     * The API is outdated and thus no inventory
     * update is made when a player levels up
     */

    public static class RPGPlayerLevelingPlayer extends RPGPlayer {
        public RPGPlayerLevelingPlayer(PlayerData playerData) {
            super(playerData);
        }

        @Override
        public int getLevel() {
            return new API().getPlayerLevel(getPlayer());
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public double getMana() {
            return new API().getMana(getPlayer());
        }

        @Override
        public double getStamina() {
            return new API().getPower(getPlayer());
        }

        @Override
        public void setMana(double value) {
            new API().setMana(getPlayer(), (int) value);
        }

        @Override
        public void setStamina(double value) {
            new API().setPower(getPlayer(), (int) value);
        }
    }
}