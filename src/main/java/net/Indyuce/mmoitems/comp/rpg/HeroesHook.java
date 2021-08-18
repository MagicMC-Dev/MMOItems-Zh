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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HeroesHook implements RPGHandler, Listener, AttackHandler {
    private final Map<SkillType, DamageType> damages = new HashMap<>();

    public HeroesHook() {
        MythicLib.plugin.getDamage().registerHandler(this);

        damages.put(SkillType.ABILITY_PROPERTY_PHYSICAL, DamageType.PHYSICAL);
        damages.put(SkillType.ABILITY_PROPERTY_MAGICAL, DamageType.MAGIC);
        damages.put(SkillType.ABILITY_PROPERTY_PROJECTILE, DamageType.PROJECTILE);
    }

    @Override
    public boolean isAttacked(Entity entity) {
        SkillUseInfo info = Heroes.getInstance().getDamageManager().getSpellTargetInfo(entity);
        return info != null && info.getCharacter().getEntity() instanceof Player;
    }

    @Override
    public AttackMetadata getAttack(Entity entity) {
        SkillUseInfo info = Heroes.getInstance().getDamageManager().getSpellTargetInfo(entity);
        Player player = (Player) info.getCharacter().getEntity();
        Set<DamageType> types = info.getSkill().getTypes().stream().filter(damages::containsKey).map(damages::get).collect(Collectors.toSet());
        DamageMetadata damageMeta = new DamageMetadata(0, types.toArray(new DamageType[0]));
        return new AttackMetadata(damageMeta, MMOPlayerData.get(player).getStatMap().cache(EquipmentSlot.MAIN_HAND));
    }

    @Override
    public void refreshStats(PlayerData data) {
        Hero hero = Heroes.getInstance().getCharacterManager().getHero(data.getPlayer());
        hero.removeMaxMana("MMOItems");
        hero.addMaxMana("MMOItems", (int) data.getStats().getStat(ItemStats.MAX_MANA));
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
        public HeroesPlayer(PlayerData playerData) {
            super(playerData);
        }

        @Override
        public int getLevel() {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            return hero.getHeroLevel();
        }

        @Override
        public String getClassName() {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            return hero.getHeroClass().getName();
        }

        @Override
        public double getMana() {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            return hero.getMana();
        }

        @Override
        public double getStamina() {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            return hero.getStamina();
        }

        @Override
        public void setMana(double value) {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            hero.setMana((int) value);
        }

        @Override
        public void setStamina(double value) {
            Hero hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
            hero.setStamina((int) value);
        }
    }
}