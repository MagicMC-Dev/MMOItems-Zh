package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class MMOItemsAPI {
    private final JavaPlugin plugin;

    /**
     * @param plugin Plugin using the API
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
     * @param skill Skill to register
     */
    public void registerSkill(RegisteredSkill skill) {
        MMOItems.plugin.getSkills().registerSkill(skill);
    }

    /**
     * @return Skill with the specified identifier like 'FIREBOLT'
     */
    public RegisteredSkill getSkillById(String id) {
        return Objects.requireNonNull(MMOItems.plugin.getSkills().getSkill(id), "Could not find skill with ID '" + id + "'");
    }

    public PlayerData getPlayerData(Player player) {
        return PlayerData.get(player);
    }

    public RPGPlayer getRPGInfo(Player player) {
        return PlayerData.get(player).getRPG();
    }

    /**
     * Forces a player to cast a skill on a specific target, with
     * custom modifiers and an attack bound to the cast skill.
     *
     * @param player     Player casting the ability
     * @param skill      Ability being cast. Can be found using {@link #getSkillById(String)}
     * @param modifiers  Ability modifiers. Can be empty; if one of the skill
     *                   modifiers is not found in that map, MMOItems will take
     *                   its default value.
     * @param target     The ability target (null if no target)
     * @param attackMeta If the trigger type is ATTACK, the corresponding AttackMetadata is provided. This allows
     *                   MMOItems to increase the damage of the current attack or even add new damage packets.
     *                   This parameter is useless for trigger types like RIGHT_CLICK or SNEAK which aren't
     * @return Result of cast skill
     */
    public SkillResult castSkill(Player player, RegisteredSkill skill, @NotNull Map<String, Double> modifiers, @Nullable LivingEntity target, @Nullable AttackMetadata attackMeta) {
        AbilityData castable = new AbilityData(skill, TriggerType.CAST);
        modifiers.forEach((mod, value) -> castable.setModifier(mod, value));

        PlayerMetadata caster = MMOPlayerData.get(player).getStatMap().cache(EquipmentSlot.MAIN_HAND);
        return castable.cast(new TriggerMetadata(caster, target, attackMeta));
    }
}
