package net.Indyuce.mmoitems.comp.mmocore;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.item.UntargetedWeaponUseEvent;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Attribute;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Profession;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

public class MMOCoreHook implements RPGHandler, Listener {

    /**
     * Called when MMOItems enables
     * <p>
     * These stats are only updated on a server reload because that
     * class has to be instantiated again for the registered stats to update
     */
    public MMOCoreHook() {
        for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
            MMOItems.plugin.getStats().register(new Required_Attribute(attribute));
        for (Profession profession : MMOCore.plugin.professionManager.getAll()) {

            // Adds profession specific Additional Experience stats.
            MMOItems.plugin.getStats().register(new DoubleStat((StatType.ADDITIONAL_EXPERIENCE.name() + '_' + profession.getId())
                .replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT),
                VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), profession.getName() + ' ' + "Additional Experience (MMOCore)"
                , new String[]{"Additional MMOCore profession " + profession.getName() + " experience in %."}, new String[]{"!block", "all"}));

            MMOItems.plugin.getStats().register(new Required_Profession(profession));
        }
    }

    @Override
    public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
    }

    @Override
    public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return new MMOCoreRPGPlayer(data);
    }

    @EventHandler
    public void updateInventoryOnLevelUp(PlayerLevelUpEvent event) {
        net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    @EventHandler
    public void updateInventoryOnClassChange(PlayerChangeClassEvent event) {
        net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    @EventHandler(ignoreCancelled = true)
    public void stopAttacksWhenCasting(UntargetedWeaponUseEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayerData().getUniqueId());
        if (playerData.isCasting())
            event.setCancelled(true);
    }

    /**
     * Removing this as it is causing issues when players log on for the first time.
     * Right after MMOCore loads the player data, MMOItems player data is not loaded yet
     * so net.Indyuce.mmoitems.api.player.PlayerData.get() returns null so this can't work
     */
	/*@EventHandler
	public void updateInventoryOnPlayerDataLoad(PlayerDataLoadEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}*/

    public static class MMOCoreRPGPlayer extends RPGPlayer {
        private final PlayerData data;

        public MMOCoreRPGPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
            super(playerData);

            data = PlayerData.get(playerData.getUniqueId());
        }

        public PlayerData getData() {
            return data;
        }

        @Override
        public int getLevel() {
            return data.getLevel();
        }

        @Override
        public String getClassName() {
            return data.getProfess().getName();
        }

        @Override
        public double getMana() {
            return data.getMana();
        }

        @Override
        public double getStamina() {
            return data.getStamina();
        }

        @Override
        public void setMana(double value) {
            data.setMana(value);
        }

        @Override
        public void setStamina(double value) {
            data.setStamina(value);
        }

        @Override
        public void giveMana(double value) {
            data.giveMana(value, PlayerResourceUpdateEvent.UpdateReason.OTHER);
        }

        @Override
        public void giveStamina(double value) {
            data.giveStamina(value, PlayerResourceUpdateEvent.UpdateReason.OTHER);
        }
    }
}