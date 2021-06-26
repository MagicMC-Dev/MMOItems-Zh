package net.Indyuce.mmoitems.comp.rpg;

import com.archyx.aureliumskills.api.event.SkillLevelUpEvent;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import net.Indyuce.mmoitems.api.player.EmptyRPGPlayer;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AureliumSkillsHook implements RPGHandler, Listener {

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

        /**
         * AureliumSkills does not load player data directly on startup, instead we have to
         * listen to the PlayerDataLoadEvent before caching the rpg player data instance.
         *
         * See PlayerDataLoadEvent event handler below.
         */
        return new EmptyRPGPlayer(data);
    }

    @EventHandler
    public void a(PlayerDataLoadEvent event) {
        Player player = event.getPlayerData().getPlayer();
        PlayerData playerData = PlayerData.get(player);
        playerData.setRPGPlayer(new AureliumSkillsPlayer(playerData, event.getPlayerData()));
    }

    public static class AureliumSkillsPlayer extends RPGPlayer {
        private final com.archyx.aureliumskills.data.PlayerData info;

        public AureliumSkillsPlayer(PlayerData playerData, com.archyx.aureliumskills.data.PlayerData rpgPlayerData) {
            super(playerData);

            info = rpgPlayerData;
        }

        @Override
        public int getLevel() {
            return info.getPowerLevel();
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public double getMana() {
            return info.getMana();
        }

        @Override
        public double getStamina() {
            return getPlayer().getFoodLevel();
        }

        @Override
        public void setMana(double value) {
            info.setMana(value);
        }

        @Override
        public void setStamina(double value) {
            getPlayer().setFoodLevel((int) value);
        }
    }
}