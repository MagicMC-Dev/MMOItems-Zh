package net.Indyuce.mmoitems.comp.rpg;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerLevelUpEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SkillAPIHook implements RPGHandler, AttackHandler {
    private final Map<Integer, AttackMetadata> damageInfo = new HashMap<>();

    public SkillAPIHook() {
        MythicLib.plugin.getDamage().registerHandler(this);
        Bukkit.getPluginManager().registerEvents(new InnerListener(), MMOItems.plugin);
    }

    @Override
    public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return new SkillAPIPlayer(data);
    }

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        return damageInfo.get(event.getEntity().getEntityId());
    }

    class InnerListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(SkillDamageEvent event) {
            if (!(event.getDamager() instanceof Player))
                return;

            DamageMetadata damageMeta = new DamageMetadata(event.getDamage(), DamageType.SKILL);
            AttackMetadata attackMeta = new AttackMetadata(damageMeta, event.getTarget(), MMOPlayerData.get(event.getDamager().getUniqueId()).getStatMap().cache(EquipmentSlot.MAIN_HAND));
            damageInfo.put(event.getTarget().getEntityId(), attackMeta);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void b(EntityDamageByEntityEvent event) {
            damageInfo.remove(event.getEntity().getEntityId());
        }
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