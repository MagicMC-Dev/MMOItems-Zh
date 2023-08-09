package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.skill.ShulkerMissile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * A HUB for skills for them to be readily available within the plugin
 */
public class SkillManager {
    private final Map<String, RegisteredSkill> skills = new HashMap<>();

    /**
     * @param id Internal name of the skill you want to fetch.
     * @return If a skill is loaded with this name, that skill.
     */
    @Nullable
    public RegisteredSkill getSkill(String id) {
        return skills.get(id);
    }

    /**
     * @param id The internal name of the skill you want to fetch.
     * @return The skill that is loaded with this name; NullPointerException if not loaded.
     */
    @NotNull
    public RegisteredSkill getSkillOrThrow(String id) {
        return Objects.requireNonNull(skills.get(id), "Could not find skill with ID '" + id + "'");
    }

    /**
     * @param skill Skill to load
     */
    public void registerSkill(RegisteredSkill skill) {
        skills.put(Objects.requireNonNull(skill, "Skill cannot be null").getHandler().getId(), skill);
    }

    /**
     * @param id Internal name of the skill you want to fetch.
     * @return If a skill of this name is loaded by the plugin.
     */
    public boolean hasSkill(String id) {
        return skills.containsKey(id);
    }

    /**
     * @return Collection of all registered skills. It has the same number
     *         of elements as MythicLib's skill handler registry.
     */
    @NotNull
    public Collection<RegisteredSkill> getAll() {
        return skills.values();
    }

    /**
     * Will load skills from MythicLib as well as generate their configuration
     * files in plugins/MMOItems/skills ~ for default values and translation.
     *
     * @param clearBefore If the previously-loaded skills should get cleared.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize(boolean clearBefore) {

        // Clear loaded skills
        if (clearBefore)
            skills.clear();

        MythicLib.plugin.getSkills().registerSkillHandler(new ShulkerMissile());

        File skillFolder = new File(MMOItems.plugin.getDataFolder() + "/skill");
        if (!skillFolder.exists()) {

            try {

                // Create folder
                skillFolder.mkdir();

                // Copy example skills
                for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {
                    InputStream res = MMOItems.plugin.getResource("default/skill/" + handler.getLowerCaseId() + ".yml");
                    if (res != null)
                        Files.copy(res, new File(MMOItems.plugin.getDataFolder() + "/skill/" + handler.getLowerCaseId() + ".yml").getAbsoluteFile().toPath());
                }

                // Should not happen
            } catch (IOException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not save default ability configs: " + exception.getMessage());
            }
        }

        // Copy MythicLib skills
        for (SkillHandler<?> handler : MythicLib.plugin.getSkills().getHandlers()) {
            ConfigFile config = new ConfigFile("/skill", handler.getLowerCaseId());
            if (!config.exists()) {
                config.getConfig().set("name", MMOUtils.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                for (String mod : handler.getModifiers()) {
                    config.getConfig().set("modifier." + mod + ".name", MMOUtils.caseOnWords(mod.replace("-", " ").toLowerCase()));
                    config.getConfig().set("modifier." + mod + ".default-value", 0);
                }
                config.save();
            }

            try {

                // Attempt to register
                skills.put(handler.getId(), new RegisteredSkill(handler, config.getConfig()));

                // Fail
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load skill '" + handler.getId() + "': " + exception.getMessage());
            }
        }
    }
}
