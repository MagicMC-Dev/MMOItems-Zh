package net.Indyuce.mmoitems.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.Ability;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisteredSkill {
    private final SkillHandler<?> handler;
    private final String name;
    private final Map<String, String> modifierNames = new HashMap<>();
    private final Map<String, Double> modifierDefaultValues = new HashMap<>();

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        this.name = Objects.requireNonNull(config.getString("name"), "Could not fill skill name");
        for (Object obj : handler.getModifiers()) {
            String mod = obj.toString();
            modifierNames.put(mod, Objects.requireNonNull(config.getString("modifier." + mod + ".name"), "Could not find translation for modifier '" + mod + "'"));
            modifierDefaultValues.put(mod, config.getDouble("modifier." + mod + ".default-value"));
        }
    }

    @Deprecated
    public RegisteredSkill(Ability ability) {
        this.handler = ability;
        this.name = MMOItems.plugin.getLanguage().getAbilityName(ability);

        for (String mod : handler.getModifiers()) {
            modifierDefaultValues.put(mod, ability.getDefaultValue(mod));
            modifierNames.put(mod, MMOUtils.caseOnWords(mod.toLowerCase().replace("_", " ").replace("-", " ")));
        }
    }

    @Deprecated
    public RegisteredSkill(SkillHandler handler, String name) {
        this.handler = handler;
        this.name = name;
    }

    public SkillHandler<?> getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public void setDefaultValue(String modifier, double value) {
        modifierDefaultValues.put(modifier, value);
    }

    @Deprecated
    public void setName(String modifier, String name) {
        modifierNames.put(modifier, name);
    }

    @Nullable
    public String getModifierName(String modifier) {
        return modifierNames.get(modifier);
    }

    public double getDefaultModifier(String modifier) {
        return modifierDefaultValues.get(modifier);
    }
}
