package net.Indyuce.mmoitems.comp.rpg;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillUseInfo;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.SkillType;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.RequiredLevelStat;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HeroesHook implements RPGHandler, Listener, AttackHandler {
    private final Map<SkillType, DamageType> damages = new HashMap<>();

    public static final ItemStat MAX_STAMINA = new DoubleStat("MAX_STAMINA", Material.EMERALD, "Max Stamina", new String[]{"Adds stamina to your max stamina bar"}), REQUIRED_SECONDARY_HERO_LEVEL = new RequiredSecondaryLevel();


    public HeroesHook() {
        MythicLib.plugin.getDamage().registerHandler(this);

        damages.put(SkillType.ABILITY_PROPERTY_PHYSICAL, DamageType.PHYSICAL);
        damages.put(SkillType.ABILITY_PROPERTY_MAGICAL, DamageType.MAGIC);
        damages.put(SkillType.ABILITY_PROPERTY_PROJECTILE, DamageType.PROJECTILE);
    }

    @NotNull
    private DamageType[] mapSkillTypes(Set<SkillType> types) {
        ArrayList<DamageType> list = new ArrayList<>();
        for (SkillType type : types) {
            final DamageType found = this.damages.get(type);
            if (found != null) list.add(found);
        }

        // Always, at least, the SKILL damage type
        list.add(DamageType.SKILL);

        return list.toArray(new DamageType[0]);
    }

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        SkillUseInfo info = Heroes.getInstance().getDamageManager().getSpellTargetInfo(event.getEntity());
        if (info == null || !(info.getCharacter().getEntity() instanceof Player)) return null;

        final Player player = (Player) info.getCharacter().getEntity();
        final DamageMetadata damageMeta = new DamageMetadata(event.getDamage(), mapSkillTypes(info.getSkill().getTypes()));
        return new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), MMOPlayerData.get(player).getStatMap().cache(EquipmentSlot.MAIN_HAND));
    }

    @Override
    public void refreshStats(PlayerData data) {
        Hero hero = Heroes.getInstance().getCharacterManager().getHero(data.getPlayer());
        hero.removeMaxMana("MMOItems");
        hero.addMaxMana("MMOItems", (int) data.getStat(ItemStats.MAX_MANA));
        hero.removeMaxStamina("MMOItems");
        hero.addMaxStamina("MMOItems", (int) data.getStat(MAX_STAMINA));

        // Backwards compatibility. Max health is operated by MythicLib
        hero.removeMaxHealth("MMOItems");
    }

    @Override
    public RPGPlayer getInfo(PlayerData data) {
        return new PlayerWrapper(data);
    }

    /**
     * Update the player's inventory whenever he levels up
     * since it could change his current stat requirements.
     */
    @EventHandler
    public void a(HeroChangeLevelEvent event) {
        PlayerData.get(event.getHero().getPlayer()).resolveModifiersLater();
    }

    /**
     * Update the player's inventory whenever he changes class
     * since it could change his current stat requirements.
     */
    @EventHandler
    public void b(ClassChangeEvent event) {
        PlayerData.get(event.getHero().getPlayer()).resolveModifiersLater();
    }

    private static class PlayerWrapper extends RPGPlayer {
        private final Hero hero;

        public PlayerWrapper(PlayerData playerData) {
            super(playerData);
            hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
        }

        @Override
        public int getLevel() {
            return hero.getHeroLevel(hero.getHeroClass());
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

    private static class RequiredSecondaryLevel extends RequiredLevelStat implements ItemRestriction {
        public RequiredSecondaryLevel() {
            super("SECONDARY_HERO_LEVEL", Material.EXPERIENCE_BOTTLE, "Secondary Hero Level", new String[]{"Secondary hero level requirement for your item."});
        }

        @Override
        public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
            final int requirement = item.getInteger(this.getNBTPath());
            if (requirement <= 0) return true;

            Validate.isTrue(player instanceof PlayerWrapper, "Cannot use secondary hero level requirement while Heroes is not the RPG provider");
            final Hero hero = ((PlayerWrapper) player).hero;
            final @Nullable HeroClass class2 = hero.getSecondaryClass();
            final int heroSecLevel = class2 == null ? 1 : hero.getHeroLevel(hero.getSecondaryClass());
            if (heroSecLevel >= requirement || player.getPlayer().hasPermission("mmoitems.bypass.level")) return true;

            if (message) {
                Message.NOT_ENOUGH_LEVELS.format(ChatColor.RED).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }
    }
}