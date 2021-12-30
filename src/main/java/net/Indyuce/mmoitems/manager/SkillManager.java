package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class SkillManager {
    private final Map<String, RegisteredSkill> skills = new HashMap<>();

    public RegisteredSkill getSkill(String id) {
        return skills.get(id);
    }

    public RegisteredSkill getSkillOrThrow(String id) {
        return Objects.requireNonNull(skills.get(id), "Could not find skill with ID '" + id + "'");
    }

    public void registerSkill(RegisteredSkill skill) {
        Validate.notNull(skill);

        this.skills.put(skill.getHandler().getId(), skill);
    }

    public boolean hasSkill(String id) {
        return skills.containsKey(id);
    }

    /**
     * @return Collection of all registered skills. It has the same number
     *         of elements as MythicLib's skill handler registry.
     */
    public Collection<RegisteredSkill> getAll() {
        return skills.values();
    }

    public void reload() {

        // Check for default files
        File skillFolder = new File(MMOItems.plugin.getDataFolder() + "/skill");
        if (!skillFolder.exists())
            try {
                skillFolder.mkdir();

                for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {
                    InputStream res = MMOItems.plugin.getResource("default/skill/" + handler.getLowerCaseId() + ".yml");
                    if (res != null)
                        Files.copy(res, new File(MMOItems.plugin.getDataFolder() + "/skill/" + handler.getLowerCaseId() + ".yml").getAbsoluteFile().toPath());
                }
            } catch (IOException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not save default ability configs: " + exception.getMessage());
            }

        for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {

            // Check if config file exists
            ConfigFile config = new ConfigFile("/skill", handler.getLowerCaseId());
            if (!config.exists()) {
                config.getConfig().set("name", MMOUtils.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                for (Object mod : handler.getModifiers()) {
                    config.getConfig().set("modifier." + mod + ".name", MMOUtils.caseOnWords(mod.toString().replace("-", " ").toLowerCase()));
                    config.getConfig().set("modifier." + mod + ".default-value", 0);
                }
                config.save();
            }

            RegisteredSkill skill = new RegisteredSkill(handler, config.getConfig());
            this.skills.put(handler.getId(), skill);
        }
    }
}
