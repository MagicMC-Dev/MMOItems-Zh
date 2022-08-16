package net.Indyuce.mmoitems.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisteredSkill {
    @NotNull private final SkillHandler<?> handler;
    @NotNull private final String name;
    @NotNull private final Map<String, String> modifierNames = new HashMap<>();
    @NotNull private final Map<String, Double> modifierDefaultValues = new HashMap<>();

    public RegisteredSkill(@NotNull SkillHandler<?> handler, @NotNull ConfigurationSection config) {
        this.handler = handler;

        this.name = Objects.requireNonNull(config.getString("name"), "Could not fill skill name");
        for (String mod : handler.getModifiers()) {
            modifierNames.put(mod, Objects.requireNonNull(config.getString("modifier." + mod + ".name"), "Could not find translation for modifier '" + mod + "'"));
            modifierDefaultValues.put(mod, config.getDouble("modifier." + mod + ".default-value"));
        }
    }

    @Deprecated
    public RegisteredSkill(@NotNull SkillHandler handler, @NotNull String name) {
        this.handler = handler;
        this.name = name;
    }

    @NotNull public SkillHandler<?> getHandler() {
        return handler;
    }

    @NotNull public String getName() {
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
