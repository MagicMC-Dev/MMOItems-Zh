package net.Indyuce.mmoitems.comp.rpg;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillUseInfo;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillType;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HeroesHook implements RPGHandler, Listener, AttackHandler {
    private final Map<SkillType, DamageType> damages = new HashMap<>();

    public static final ItemStat MAX_STAMINA = new DoubleStat("MAX_STAMINA", Material.EMERALD, "Max Stamina", new String[]{"Adds stamina to your max stamina bar"});

    public HeroesHook() {
        MythicLib.plugin.getDamage().registerHandler(this);

        damages.put(SkillType.ABILITY_PROPERTY_PHYSICAL, DamageType.PHYSICAL);
        damages.put(SkillType.ABILITY_PROPERTY_MAGICAL, DamageType.MAGIC);
        damages.put(SkillType.ABILITY_PROPERTY_PROJECTILE, DamageType.PROJECTILE);
    }

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        SkillUseInfo info = Heroes.getInstance().getDamageManager().getSpellTargetInfo(event.getEntity());
        if (info == null || !(info.getCharacter().getEntity() instanceof Player))
            return null;

        Player player = (Player) info.getCharacter().getEntity();
        DamageMetadata damageMeta = new DamageMetadata(event.getDamage(), info.getSkill().getTypes().stream().filter(damages::containsKey).map(damages::get).toArray(DamageType[]::new));
        return new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), MMOPlayerData.get(player).getStatMap().cache(EquipmentSlot.MAIN_HAND));
    }

    @Override
    public void refreshStats(PlayerData data) {
        Hero hero = Heroes.getInstance().getCharacterManager().getHero(data.getPlayer());
        hero.removeMaxMana("MMOItems");
        hero.addMaxMana("MMOItems", (int) data.getStats().getStat(ItemStats.MAX_MANA));
        hero.removeMaxStamina("MMOItems");
        hero.addMaxStamina("MMOItems", (int) data.getStats().getStat(MAX_STAMINA));
        hero.removeMaxHealth("MMOItems");
        hero.addMaxHealth("MMOItems", data.getStats().getStat(ItemStats.MAX_HEALTH));
    }

    @Override
    public RPGPlayer getInfo(PlayerData data) {
        return new HeroesPlayer(data);
    }

    /**
     * Update the player's inventory whenever he levels up
     * since it could change his current stat requirements.
     */
    @EventHandler
    public void a(HeroChangeLevelEvent event) {
        PlayerData.get(event.getHero().getPlayer()).getInventory().scheduleUpdate();
    }

    /**
     * Update the player's inventory whenever he changes class
     * since it could change his current stat requirements.
     */
    @EventHandler
    public void b(ClassChangeEvent event) {
        PlayerData.get(event.getHero().getPlayer()).getInventory().scheduleUpdate();
    }

    public static class HeroesPlayer extends RPGPlayer {
        private final Hero hero;

        public HeroesPlayer(PlayerData playerData) {
            super(playerData);
            hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
        }

        @Override
        public int getLevel() {
            return hero.getHeroLevel();
        }

        @Override
        public String getClassName() {
            return hero.getHeroClass().getName();
        }

        @Override
        public double getMana() {
            return hero.getMana();
        }

        @Override
        public double getStamina() {
            return hero.getStamina();
        }

        @Override
        public void setMana(double value) {
            hero.setMana((int) value);
        }

        @Override
        public void setStamina(double value) {
            hero.setStamina((int) value);
        }
    }
}