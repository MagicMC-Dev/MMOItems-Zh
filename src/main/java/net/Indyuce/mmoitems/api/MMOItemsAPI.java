package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class MMOItemsAPI {
    private final JavaPlugin plugin;

    /**
     * @param plugin Plugin
     */
    public MMOItemsAPI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers an ability in MMOItems. This must be called before MMOItems enables,
     * therefore either using a loadbefore of MMOItems and while the plugin enables,
     * or using a dependency and usign #onLoad().
     * <p>
     * This method does NOT register listeners.
     * <p>
     * Throws an IAE if anything goes wrong.
     *
     * @param ability Ability to register
     */
    @Deprecated
    public void registerAbility(Ability ability) {
        MMOItems.plugin.getAbilities().registerAbility(ability);
    }

    /**
     * @return Ability with the specified identifier like FIREBOLT
     */
    public RegisteredSkill getAbilityById(String id) {
        return Objects.requireNonNull(MMOItems.plugin.getSkills().getSkill(id), "Could not find skill with ID '" + id + "'");
    }

    public PlayerData getPlayerData(Player player) {
        return PlayerData.get(player);
    }

    public RPGPlayer getRPGInfo(Player player) {
        return PlayerData.get(player).getRPG();
    }

    /**
     * Forces a player to cast an ability
     *
     * @param player      Player casting the ability
     * @param abilityName Ability name. The ability is found using {@link #getAbilityById(String)}
     * @param modifiers   Ability modifiers
     * @param target      The ability target (null if no target)
     */
    public AttackMetadata castAbility(Player player, String abilityName, Map<String, Double> modifiers, @NotNull LivingEntity target) {
        AttackMetadata attackMeta = new AttackMetadata(new DamageMetadata(), MMOPlayerData.get(player).getStatMap().cache(EquipmentSlot.MAIN_HAND));
        return castAbility(player, abilityName, modifiers, target, attackMeta);
    }

    /**
     * Forces a player to cast an ability
     *
     * @param player      Player casting the ability
     * @param abilityName Ability name. The ability is found using {@link #getAbilityById(String)}
     * @param modifiers   Ability modifiers
     * @param target      The ability target (null if no target)
     * @param attackMeta  If the trigger type is ATTACK, the corresponding AttackMetadata is provided. This allows
     *                    MMOItems to increase the damage of the current attack or even add new damage packets.
     *                    This parameter is useless for trigger types like RIGHT_CLICK or SNEAK which aren't
     *                    based on entity attacks; in that case the attackMeta will have an empty DamageMetadata
     */
    @Deprecated
    public AttackMetadata castAbility(Player player, String abilityName, Map<String, Double> modifiers, @NotNull LivingEntity target, AttackMetadata attackMeta) {

        // Setup ability
        AbilityData abilityData = new AbilityData(getAbilityById(abilityName), TriggerType.RIGHT_CLICK);
        modifiers.forEach((key, value) -> abilityData.setModifier(key, value));

        // Cast ability
        return castAbility(PlayerData.get(player), abilityData, target, attackMeta);
    }

    /**
     * Forces a player to cast an ability
     *
     * @param playerData The player data from the player casting the spell
     * @param ability    Ability with modifiers
     * @param target     The ability target (null if no target)
     * @param attackMeta If the trigger type is ATTACK, the corresponding AttackMetadata is provided. This allows
     *                   MMOItems to increase the damage of the current attack or even add new damage packets.
     *                   This parameter is useless for trigger types like RIGHT_CLICK or SNEAK which aren't
     *                   based on entity attacks; in that case the attackMeta will have an empty DamageMetadata
     */
    @Deprecated
    public AttackMetadata castAbility(PlayerData playerData, AbilityData ability, @NotNull LivingEntity target, AttackMetadata attackMeta) {
        playerData.cast(attackMeta, target, ability);
        return attackMeta;
    }
}
