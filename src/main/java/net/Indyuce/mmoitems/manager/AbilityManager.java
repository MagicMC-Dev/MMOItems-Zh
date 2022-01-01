package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.ability.BackwardsCompatibleAbility;
import net.Indyuce.mmoitems.skill.RegisteredSkill;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Deprecated
public class AbilityManager {
    public Ability getAbility(String id) {
        return new BackwardsCompatibleAbility(MMOItems.plugin.getSkills().getSkillOrThrow(id));
    }

    public boolean hasAbility(String id) {
        return MMOItems.plugin.getSkills().hasSkill(id);
    }

    /**
     * @return Collection of all active abilities
     */
    public Collection<Ability> getAll() {
        return MMOItems.plugin.getSkills().getAll().stream().map(skill -> new BackwardsCompatibleAbility(skill)).collect(Collectors.toSet());
    }

    /**
     * Add multiple abilities at the same time
     * but for multiple abilities.
     *
     * @param abilities - Refer to {@link #registerAbility(Ability ability)}
     */
    public void registerAbilities(Ability... abilities) {
        for (Ability ability : abilities)
            registerAbility(ability);
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
    public void registerAbility(Ability ability) {
        MMOItems.plugin.getSkills().registerSkill(new RegisteredSkill(ability));
    }
}
