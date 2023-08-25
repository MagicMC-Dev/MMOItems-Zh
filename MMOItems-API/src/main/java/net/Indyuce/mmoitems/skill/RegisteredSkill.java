package net.Indyuce.mmoitems.skill;

import io.lumine.mythic.lib.UtilityMethods;
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
    @NotNull private final Map<String, String> parameterNames = new HashMap<>();
    @NotNull private final Map<String, Double> defaultParameterValues = new HashMap<>();

    public RegisteredSkill(@NotNull SkillHandler<?> handler, @NotNull ConfigurationSection config) {
        this.handler = handler;

        this.name = Objects.requireNonNull(config.getString("name"), "Could not fill skill name");
        for (String mod : handler.getModifiers()) {
            parameterNames.put(mod, config.getString("modifier." + mod + ".name", UtilityMethods.caseOnWords(mod.replace("_", " ").replace("-", " ").toLowerCase())));
            defaultParameterValues.put(mod, config.getDouble("modifier." + mod + ".default-value"));
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
        defaultParameterValues.put(modifier, value);
    }

    @Deprecated
    public void setName(String modifier, String name) {
        parameterNames.put(modifier, name);
    }

    @Nullable
    public String getParameterName(String modifier) {
        return parameterNames.get(modifier);
    }

    public double getDefaultModifier(String modifier) {
        return defaultParameterValues.get(modifier);
    }
}
