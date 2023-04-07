package net.Indyuce.mmoitems.comp.rpg;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.api.event.SkillLevelUpEvent;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stats;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.EmptyRPGPlayer;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.RequiredLevelStat;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AureliumSkillsHook implements RPGHandler, Listener {
    private final AureliumSkills aSkills;

    private final Map<Stats, ItemStat> statExtra = new HashMap<>();

    public AureliumSkillsHook() {
        aSkills = (AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");

        for (Stats stat : Stats.values()) {
            final String statName = UtilityMethods.caseOnWords(stat.name().toLowerCase());
            final ItemStat miStat = new DoubleStat("ADDITIONAL_" + stat.name(), Material.BOOK,
                    "Additional " + statName,
                    new String[]{"Additional " + statName + " (AureliumSkills)"},
                    new String[]{"!miscellaneous", "!block", "all"});

            statExtra.put(stat, miStat);
            MMOItems.plugin.getStats().register(miStat);
        }

        // Register stat for required professions
        for (Skills skill : Skills.values())
            MMOItems.plugin.getStats().register(new RequiredProfessionStat(skill));
    }

    @EventHandler
    public void a(SkillLevelUpEvent event) {
        OfflinePlayer player = event.getPlayer();
        if (player.isOnline())
            PlayerData.get(player).getInventory().scheduleUpdate();
    }

    /**
     * AureliumSkills stores modifiers using ONE hash map for every stat
     * unlike MythicLib which has several stat instances. Therefore, a
     * valid key for a stat modifier is "mmoitems_<stat_name>".
     * <p>
     * Be careful, ASkills permanently stores modifiers unlike ML
     */
    private static final String MODIFIER_KEY_PREFIX = "mmoitems_";

    @Override
    public void refreshStats(PlayerData data) {
        statExtra.forEach((stat, miStat) -> AureliumAPI.addStatModifier(data.getPlayer(), MODIFIER_KEY_PREFIX + stat.name(), stat, data.getStats().getStat(miStat)));
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

    public class AureliumSkillsPlayer extends RPGPlayer {
        private final com.archyx.aureliumskills.data.PlayerData info;

        public AureliumSkillsPlayer(PlayerData playerData, com.archyx.aureliumskills.data.PlayerData rpgPlayerData) {
            super(playerData);

            info = rpgPlayerData;
        }

        public com.archyx.aureliumskills.data.PlayerData getAureliumSkillsPlayerData() {
            return info;
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

    public class RequiredProfessionStat extends RequiredLevelStat {
        private final Skill skill;

        public RequiredProfessionStat(Skills skill) {
            super(skill.name(), VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), skill.getDisplayName(Locale.getDefault()),
                    new String[]{"Amount of " + skill.getDisplayName(Locale.getDefault()) + " levels the", "player needs to use the item.", "(AureliumSkills)"});

            this.skill = aSkills.getSkillRegistry().getSkill(skill.name());
        }

        @Override
        public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {

            final int skillLevel = AureliumAPI.getSkillLevel(player.getPlayer(), skill);
            final int required = item.getInteger("MMOITEMS_REQUIRED_" + skill.name());

            if (skillLevel < required && !player.getPlayer().hasPermission("mmoitems.bypass.level")) {
                if (message) {
                    Message.NOT_ENOUGH_PROFESSION.format(ChatColor.RED, "#profession#", skill.getDisplayName(Locale.getDefault())).send(player.getPlayer());
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
                }
                return false;
            }

            return true;
        }
    }
}